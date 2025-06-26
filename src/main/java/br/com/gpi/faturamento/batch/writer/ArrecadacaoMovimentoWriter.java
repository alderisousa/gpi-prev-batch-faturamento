package br.com.gpi.faturamento.batch.writer;

import br.com.gpi.faturamento.domain.Arrecadacao;
import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import br.com.gpi.faturamento.domain.PlanoProcessamento;
import br.com.gpi.faturamento.repository.ArrecadacaoMovimentoRepository;
import br.com.gpi.faturamento.repository.ArrecadacaoRepository;
import br.com.gpi.faturamento.service.PlanoProcessamentoService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ArrecadacaoMovimentoWriter implements ItemWriter<PlanoProcessamento> {


    @Autowired
    private PlanoProcessamentoService planoProcessamentoService;

    @Autowired
    private ArrecadacaoRepository arrecadacaoRepository;

    @Autowired
    private ArrecadacaoMovimentoRepository arrecadacaoMovimentoRepository;

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

            Map<String, Arrecadacao> mapArrecadacao = new HashMap<>();
            for (var arrecadacaoMovimento : processamento.getMovimentos()) {
                if (!mapArrecadacao.containsKey(getKey(arrecadacaoMovimento))) {
                    Arrecadacao arrecadacao = getArrecadacao(arrecadacoes, arrecadacaoMovimento);
                    mapArrecadacao.put(getKey(arrecadacaoMovimento), arrecadacao);
                } else {
                    Arrecadacao arrecadacao = mapArrecadacao.get(getKey(arrecadacaoMovimento));
                    ArrecadacaoMovimento movimento = getArrecadacaoMovimento(movimentos, arrecadacaoMovimento);
                    arrecadacao.getMovimentos().add(movimento);

                    arrecadacao.setValorCalculado(arrecadacao.getValorCalculado().add(movimento.getValorCalculado()));
                    arrecadacao.setValorRecebido(arrecadacao.getValorRecebido().add(movimento.getValorRecebido()));
                    arrecadacao.setValorFinal(arrecadacao.getValorFinal().add(movimento.getValorFinal()));
                }
            }
            salvarArrecadacoes(mapArrecadacao);
            atualizarPlanoProcessamento(processamento);
        }
    }

    private ArrecadacaoMovimento getArrecadacaoMovimento(List<ArrecadacaoMovimento> movimentos, ArrecadacaoMovimento arrecadacaoMovimento) {
        return movimentos.stream()
                .filter(m -> m.getIdContribuicao().equals(arrecadacaoMovimento.getIdContribuicao())
                    && m.getIdPessoaPlano().equals(arrecadacaoMovimento.getIdPessoaPlano())
                    && Optional.ofNullable(m.getIdPerfilInvestimento()).equals(Optional.ofNullable(arrecadacaoMovimento.getIdPerfilInvestimento()))
                )
                .findFirst()
                .orElse(arrecadacaoMovimento);
    }

    private Arrecadacao getArrecadacao(List<Arrecadacao> arrecadacoes, ArrecadacaoMovimento arrecadacaoMovimento) {
        return arrecadacoes.stream()
                .filter(a -> a.getIdContribuicao().equals(arrecadacaoMovimento.getIdContribuicao())
                        && Optional.ofNullable(a.getIdPerfilInvestimento()).equals(Optional.ofNullable(arrecadacaoMovimento.getIdPerfilInvestimento()))
                )
                .map(Arrecadacao::zerarValores)
                .findFirst()
                .orElse(new Arrecadacao(arrecadacaoMovimento));
    }

    private String getKey(ArrecadacaoMovimento arrecadacaoMovimento) {
        Long perfilInvestimento = Optional.ofNullable(arrecadacaoMovimento.getIdPerfilInvestimento()).orElse(0L);
        return arrecadacaoMovimento.getIdContribuicao() + "_" + perfilInvestimento;
    }

    private void atualizarPlanoProcessamento(PlanoProcessamento processamento) {
        planoProcessamentoService.atualizarParaProcessado(processamento);
    }

    @Transactional
    private void salvarArrecadacoes(Map<String, Arrecadacao> arrecadacoes) {
        for (Map.Entry<String, Arrecadacao> longArrecadacaoEntry : arrecadacoes.entrySet()) {
            Arrecadacao arrecadacao = longArrecadacaoEntry.getValue();
            Arrecadacao arrecadacaoSalva = arrecadacaoRepository.save(arrecadacao);

            arrecadacao.getMovimentos().forEach(m -> m.setIdArrecadacao(arrecadacaoSalva.getId()));

            arrecadacaoMovimentoRepository.saveAll(arrecadacao.getMovimentos());
        }
    }

}
