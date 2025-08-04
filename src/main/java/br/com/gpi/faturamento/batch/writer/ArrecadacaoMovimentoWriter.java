package br.com.gpi.faturamento.batch.writer;

import br.com.gpi.faturamento.domain.*;
import br.com.gpi.faturamento.repository.ArrecadacaoCargaRepository;
import br.com.gpi.faturamento.repository.ArrecadacaoMovimentoRepository;
import br.com.gpi.faturamento.repository.ArrecadacaoRepository;
import br.com.gpi.faturamento.service.PlanoProcessamentoService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class ArrecadacaoMovimentoWriter implements ItemWriter<PlanoProcessamento> {


    @Autowired
    private PlanoProcessamentoService planoProcessamentoService;

    @Autowired
    private ArrecadacaoRepository arrecadacaoRepository;

    @Autowired
    private ArrecadacaoMovimentoRepository arrecadacaoMovimentoRepository;

    @Autowired
    private ArrecadacaoCargaRepository arrecadacaoCargaRepository;

    @Override
    public void write(Chunk<? extends PlanoProcessamento> chunk) throws Exception {

        for (PlanoProcessamento processamento : chunk) {

            List<Arrecadacao> arrecadacoes = arrecadacaoRepository.listar(
                    processamento.getIdPessoaJuridica(),
                    processamento.getMesReferencia(),
                    processamento.getAnoReferencia());

            List<ArrecadacaoMovimento> movimentos = arrecadacaoMovimentoRepository.listar(
                    processamento.getIdPessoaJuridica(),
                    processamento.getMesReferencia(),
                    processamento.getAnoReferencia());

            List<ArrecadacaoCarga> recebidos = arrecadacaoCargaRepository.listar(
                    processamento.getIdPessoaJuridica(),
                    processamento.getMesReferencia(),
                    processamento.getAnoReferencia());

            List<ArrecadacaoCarga> recebidosAux = new ArrayList<>();

            arrecadacaoRepository.saveAll(arrecadacoes);
            arrecadacaoMovimentoRepository.saveAll(movimentos);

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
                    movimento.setValorFinal(recebido.get().getValorRecebido());
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

    private void adicionaRecebidosNaoCalculado(Map<String, Arrecadacao> mapArrecadacao,
                                               List<ArrecadacaoCarga> recebidos,
                                               List<ArrecadacaoMovimento> movimentos) {

        for (var arrecadacao : recebidos) {

            Optional<ArrecadacaoMovimento> optMovimento = getArrecadacaoMovimento(movimentos, arrecadacao);

            String key = getKey(arrecadacao);
            if (mapArrecadacao.containsKey(key)) {
                ArrecadacaoMovimento movimento = optMovimento.orElse(new ArrecadacaoMovimento(arrecadacao));
                movimento.setValorRecebido(arrecadacao.getValorRecebido());
                movimento.setValorFinal(arrecadacao.getValorRecebido());
                mapArrecadacao.get(key).getMovimentos().add(movimento);
            }
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
                    && Optional.ofNullable(m.getIdPerfilInvestimento()).equals(Optional.ofNullable(key.getIdPerfilInvestimento()))
                )
                .findFirst();
    }

    private Optional<ArrecadacaoCarga> getArrecadacaoCarga(List<ArrecadacaoCarga> movimentos, ArrecadacaoMovimento arrecadacaoMovimento) {
        return movimentos.stream()
                .filter(m -> m.getIdContribuicao().equals(arrecadacaoMovimento.getIdContribuicao())
                        && m.getIdPessoaPlano().equals(arrecadacaoMovimento.getIdPessoaPlano())
                        && Optional.ofNullable(m.getIdPerfilInvestimento()).equals(Optional.ofNullable(arrecadacaoMovimento.getIdPerfilInvestimento()))
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

            arrecadacaoMovimentoRepository.saveAll(arrecadacao.getMovimentos());
        }

        arrecadacaoRepository.atualizarValores(processamento.getIdPlano(),
                processamento.getMesReferencia(), processamento.getAnoReferencia());
    }

}
