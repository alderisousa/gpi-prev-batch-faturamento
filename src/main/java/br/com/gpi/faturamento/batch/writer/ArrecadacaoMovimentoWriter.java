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
            Map<String, Arrecadacao> mapArrecadacao = new HashMap<>();
            for (var arrecadacaoMovimento : processamento.getMovimentos()) {
                if (!mapArrecadacao.containsKey(getKey(arrecadacaoMovimento))) {
                    mapArrecadacao.put(getKey(arrecadacaoMovimento), new Arrecadacao(arrecadacaoMovimento));
                } else {
                    Arrecadacao arrecadacao = mapArrecadacao.get(getKey(arrecadacaoMovimento));
                    arrecadacao.getMovimentos().add(arrecadacaoMovimento);
                    arrecadacao.setValorNominal(arrecadacao.getValorNominal().add(arrecadacaoMovimento.getValorNominal()));
                }
            }
            salvarArrecadacoes(mapArrecadacao);
            atualizarPlanoProcessamento(processamento);
        }
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
