package br.com.gpi.faturamento.batch.processor;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.domain.Distribuicao;
import br.com.gpi.faturamento.domain.PlanoProcessamento;
import br.com.gpi.faturamento.repository.DadosParticipanteRepository;
import br.com.gpi.faturamento.repository.DistribuicaoRepository;
import br.com.gpi.faturamento.strategy.calculo.RotinaCalculoStrategyFactory;
import br.com.gpi.faturamento.strategy.elegibilidade.RotinaElegibilidadeStrategy;
import br.com.gpi.faturamento.utils.NumberUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class FaturamentoProcessor implements ItemProcessor<PlanoProcessamento, PlanoProcessamento> {

    @Autowired
    private DadosParticipanteRepository dadosParticipanteRepository;

    @Autowired
    private RotinaCalculoStrategyFactory rotinaCalculoStrategyFactory;

    @Autowired
    private RotinaElegibilidadeStrategy rotinaElegibilidadeStrategy;

    @Autowired
    private DistribuicaoRepository distribuicaoRepository;

    @Override
    public PlanoProcessamento process(PlanoProcessamento item) throws Exception {

        List<DadosParticipante> dadosParticipantes = dadosParticipanteRepository.listarParticipantes(item.getIdPessoaJuridica(), item.getIdPlano());

        List<Distribuicao> distribuicoes = listarDistribuicao(dadosParticipantes);

        List<ArrecadacaoMovimento> faturamentos = new ArrayList<>();
        for (DadosParticipante dados : dadosParticipantes) {

            boolean isElegivel = true;
            if (dados.getNmRotinaElegibilidade() != null) {
                isElegivel = rotinaElegibilidadeStrategy.executar(dados);
            }

            if (isElegivel && dados.getNmRotinaCalculo() != null) {
                BigDecimal valor = rotinaCalculoStrategyFactory
                        .getStrategy(dados.getNmRotinaCalculo())
                        .executar(faturamentos, dados);

                if (valor.compareTo(BigDecimal.ZERO) > 0) {

                    faturamentos.addAll(distribuirInvestimento(distribuicoes, item, dados, valor));

                    System.out.println("Pessoa " + dados.getIdPessoa() + " - " + dados.getNmContribuicao() + ": " + valor);
                }
            }
        }

        item.setMovimentos(faturamentos);
        return item;
    }

    private List<Distribuicao> listarDistribuicao(List<DadosParticipante> dadosParticipantes) {
        List<Long> idsPessoaContribuicao = dadosParticipantes.stream().map(DadosParticipante::getIdPessoaContribuicao).toList();
        return distribuicaoRepository.findAllByIdPessoaContribuicaoIn(idsPessoaContribuicao);
    }

    private List<ArrecadacaoMovimento> distribuirInvestimento(
            List<Distribuicao> distribuicoes,
            PlanoProcessamento item,
            DadosParticipante dados,
            BigDecimal valorContribuicao) {

        List<ArrecadacaoMovimento> faturamentos = new ArrayList<>();
        List<Distribuicao> distribuicoesPessoa = distribuicoes.stream()
                .filter(d -> d.getIdPessoaContribuicao().equals(dados.getIdPessoaContribuicao())
                        && d.getIdPessoaPlano().equals(dados.getIdPessoaPlano())).toList();

        if ("S".equals(dados.getDistribuicaoFlag()) && !distribuicoesPessoa.isEmpty()) {
            for (var distribuicao : distribuicoesPessoa) {
                ArrecadacaoMovimento arrecadacao = new ArrecadacaoMovimento(dados);
                arrecadacao.setValorNominal(NumberUtils.multiplicarValor(valorContribuicao,
                        NumberUtils.percentualParaFaixa(distribuicao.getPercentualDistribuicao())));
                arrecadacao.setAnoReferencia(item.getAnoReferencia());
                arrecadacao.setMesReferencia(item.getMesReferencia());
                arrecadacao.setIdPerfilInvestimento(distribuicao.getIdPerfilInvestimento());
                faturamentos.add(arrecadacao);
            }
        } else {
            ArrecadacaoMovimento arrecadacao = new ArrecadacaoMovimento(dados);
            arrecadacao.setValorNominal(valorContribuicao);
            arrecadacao.setAnoReferencia(item.getAnoReferencia());
            arrecadacao.setMesReferencia(item.getMesReferencia());
            faturamentos.add(arrecadacao);
        }

        return faturamentos;
    }

}
