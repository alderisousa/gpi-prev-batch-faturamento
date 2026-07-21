package br.com.gpi.faturamento.batch.writer;

import br.com.gpi.faturamento.domain.*;
import br.com.gpi.faturamento.repository.ArrecadacaoCargaRepository;
import br.com.gpi.faturamento.repository.ArrecadacaoMovimentoHistoricoRepository;
import br.com.gpi.faturamento.repository.ArrecadacaoMovimentoRepository;
import br.com.gpi.faturamento.repository.ArrecadacaoRepository;
import br.com.gpi.faturamento.service.PlanoProcessamentoService;
import br.com.gpi.faturamento.utils.DateUtils;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Component
public class ArrecadacaoMovimentoWriter implements ItemWriter<PlanoProcessamento> {

    private static final String FLAG_SIM = "S";

    @Autowired
    private PlanoProcessamentoService planoProcessamentoService;

    @Autowired
    private ArrecadacaoRepository arrecadacaoRepository;

    @Autowired
    private ArrecadacaoMovimentoRepository arrecadacaoMovimentoRepository;

    @Autowired
    private ArrecadacaoCargaRepository arrecadacaoCargaRepository;

    @Autowired
    private ArrecadacaoMovimentoHistoricoRepository arrecadacaoMovimentoHistoricoRepository;

    @Override
    public void write(Chunk<? extends PlanoProcessamento> chunk) throws Exception {

        for (PlanoProcessamento processamento : chunk) {

            List<Arrecadacao> arrecadacoes = arrecadacaoRepository.listar(
                    processamento.getIdPessoaJuridica(),
                    processamento.getMesReferencia(),
                    processamento.getAnoReferencia());
            arrecadacaoRepository.saveAll(arrecadacoes);

            arrecadacaoMovimentoRepository.removerPorAnoMesReferencia(
                    processamento.getIdPessoaJuridica(),
                    processamento.getMesReferencia(),
                    processamento.getAnoReferencia(),
                    DateUtils.getCompetencia(processamento.getAnoReferencia(), processamento.getMesReferencia()));

            List<ArrecadacaoCarga> recebidos = arrecadacaoCargaRepository.listar(
                    processamento.getIdPessoaJuridica(),
                    processamento.getMesReferencia(),
                    processamento.getAnoReferencia());

            List<ArrecadacaoCarga> recebidosAux = new ArrayList<>();

            List<ArrecadacaoMovimento> movimentos = new ArrayList<>();

            Map<String, Arrecadacao> mapArrecadacao = new HashMap<>();
            for (var arrecadacaoMovimento : processamento.getMovimentos()) {
                ArrecadacaoMovimento movimento = getArrecadacaoMovimento(movimentos, arrecadacaoMovimento);
                Arrecadacao arrecadacao;
                if (!mapArrecadacao.containsKey(getKey(arrecadacaoMovimento))) {
                    arrecadacao = getArrecadacao(arrecadacoes, arrecadacaoMovimento);
                    mapArrecadacao.put(getKey(arrecadacaoMovimento), arrecadacao);
                } else {
                    arrecadacao = mapArrecadacao.get(getKey(arrecadacaoMovimento));
                }

                Optional<ArrecadacaoCarga> recebido = getArrecadacaoCarga(recebidos, arrecadacaoMovimento);
                if (recebido.isPresent()) {
                    recebidosAux.add(recebido.get());
                    movimento.setValorRecebido(recebido.get().getValorRecebido());
                    setValorFinal(movimento, recebido.get().getValorRecebido());
                } else {
                    setValorFinal(movimento, arrecadacaoMovimento.getValorCalculado());
                }
                movimento.setValorCalculado(arrecadacaoMovimento.getValorCalculado());
                arrecadacao.getMovimentos().add(movimento);
            }
            recebidos.removeAll(recebidosAux);
            adicionaRecebidosNaoCalculado(mapArrecadacao, recebidos, movimentos);
            salvarArrecadacoes(mapArrecadacao, processamento);
            atualizarPlanoProcessamento(processamento);
        }
    }

    private void setValorFinal(ArrecadacaoMovimento movimento, BigDecimal valor) {
        if (FLAG_SIM.equals(movimento.getContribuicaoFinalFlag()) 
            && (movimento.getValorFinal() == null 
                || movimento.getValorFinal().compareTo(BigDecimal.ZERO) == 0)) {
            movimento.setValorFinal(valor);
        }
    }

    private void adicionaRecebidosNaoCalculado(Map<String, Arrecadacao> mapArrecadacao,
                                               List<ArrecadacaoCarga> recebidos,
                                               List<ArrecadacaoMovimento> movimentos) {

        for (var arrecadacao : recebidos) {

            ArrecadacaoMovimento movimento = getArrecadacaoMovimento(movimentos, arrecadacao).orElse(new ArrecadacaoMovimento(arrecadacao));

            String key = getKey(arrecadacao);
            if (!mapArrecadacao.containsKey(key)) {
                mapArrecadacao.put(key, new Arrecadacao(movimento));
            }

            movimento.setValorRecebido(arrecadacao.getValorRecebido());
            movimento.setValorFinal(arrecadacao.getValorRecebido());
            mapArrecadacao.get(key).getMovimentos().add(movimento);

        }
    }

    private ArrecadacaoMovimento getArrecadacaoMovimento(List<ArrecadacaoMovimento> movimentos, ArrecadacaoMovimento arrecadacaoMovimento) {
        return getArrecadacaoMovimento(movimentos, (ArrecadacaoMovimentoKey)arrecadacaoMovimento)
                .orElse(arrecadacaoMovimento);
    }

    private Optional<ArrecadacaoMovimento> getArrecadacaoMovimento(List<ArrecadacaoMovimento> movimentos, ArrecadacaoMovimentoKey key) {
        return movimentos.stream()
                .filter(m -> m.getIdContribuicao().equals(key.getIdContribuicao())
                    && m.getIdPessoaPlano().equals(key.getIdPessoaPlano())
                )
                .findFirst();
    }

    private Optional<ArrecadacaoCarga> getArrecadacaoCarga(List<ArrecadacaoCarga> movimentos, ArrecadacaoMovimento arrecadacaoMovimento) {
        return movimentos.stream()
                .filter(m -> m.getIdContribuicao().equals(arrecadacaoMovimento.getIdContribuicao())
                        && m.getIdPessoaPlano().equals(arrecadacaoMovimento.getIdPessoaPlano())
                )
                .findFirst();
    }

    private Arrecadacao getArrecadacao(List<Arrecadacao> arrecadacoes, ArrecadacaoMovimento arrecadacaoMovimento) {
        return arrecadacoes.stream()
                .filter(a -> a.getIdContribuicao().equals(arrecadacaoMovimento.getIdContribuicao())
                        && Optional.ofNullable(a.getIdPerfilInvestimento()).equals(Optional.ofNullable(arrecadacaoMovimento.getIdPerfilInvestimento()))
                )
                .findFirst()
                .orElse(new Arrecadacao(arrecadacaoMovimento));
    }

    private String getKey(ArrecadacaoMovimentoKey key) {
        Long perfilInvestimento = Optional.ofNullable(key.getIdPerfilInvestimento()).orElse(0L);
        return key.getIdContribuicao() + "_" + perfilInvestimento;
    }

    private void atualizarPlanoProcessamento(PlanoProcessamento processamento) {
        planoProcessamentoService.atualizarParaProcessado(processamento);
    }

    @Transactional
    private void salvarArrecadacoes(Map<String, Arrecadacao> arrecadacoes, PlanoProcessamento processamento) {
        for (Map.Entry<String, Arrecadacao> longArrecadacaoEntry : arrecadacoes.entrySet()) {
            Arrecadacao arrecadacao = longArrecadacaoEntry.getValue();
            Arrecadacao arrecadacaoSalva = arrecadacaoRepository.save(arrecadacao);

            arrecadacao.getMovimentos().forEach(m -> m.setIdArrecadacao(arrecadacaoSalva.getId()));

            List<ArrecadacaoMovimentoHistorico> historicos = arrecadacao.getMovimentos().stream()
                    .map(ArrecadacaoMovimentoHistorico::new)
                    .peek(h -> h.setLogin(processamento.getLogin()))
                    .toList();

            arrecadacaoMovimentoRepository.saveAll(arrecadacao.getMovimentos());
            arrecadacaoMovimentoHistoricoRepository.saveAll(historicos);
        }

        arrecadacaoRepository.atualizarValores(processamento.getIdPlano(),
                processamento.getMesReferencia(), processamento.getAnoReferencia());
    }

}
