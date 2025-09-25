package br.com.gpi.faturamento.batch.processor;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.domain.Distribuicao;
import br.com.gpi.faturamento.domain.PlanoProcessamento;
import br.com.gpi.faturamento.repository.DadosParticipanteRepository;
import br.com.gpi.faturamento.repository.DistribuicaoRepository;
import br.com.gpi.faturamento.strategy.calculo.RotinaCalculoStrategyFactory;
import br.com.gpi.faturamento.strategy.elegibilidade.RotinaElegibilidadeStrategyFactory;
import br.com.gpi.faturamento.utils.DateUtils;
import br.com.gpi.faturamento.utils.NumberUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class FaturamentoProcessor implements ItemProcessor<PlanoProcessamento, PlanoProcessamento> {

    @Autowired
    private DadosParticipanteRepository dadosParticipanteRepository;

    @Autowired
    private RotinaCalculoStrategyFactory rotinaCalculoStrategyFactory;

    @Autowired
    private RotinaElegibilidadeStrategyFactory rotinaElegibilidadeFactoryStrategy;

    @Autowired
    private DistribuicaoRepository distribuicaoRepository;

    @Override
    public PlanoProcessamento process(PlanoProcessamento item) {

        LocalDate dataCompetencia = DateUtils.getCompetencia(item.getAnoReferencia(), item.getMesReferencia());

        List<DadosParticipante> dadosParticipantes = dadosParticipanteRepository.listarParticipantes(item.getIdPessoaJuridica(), item.getIdPlano(), dataCompetencia);

        List<Distribuicao> distribuicoes = listarDistribuicao(item, dataCompetencia);

        List<ArrecadacaoMovimento> faturamentos = new ArrayList<>();
        for (DadosParticipante dados : dadosParticipantes) {

            boolean isElegivel = true;
            if (dados.getNmRotinaElegibilidade() != null) {
                isElegivel = rotinaElegibilidadeFactoryStrategy
                        .getStrategy(dados.getNmRotinaElegibilidade())
                        .executar(dados);
            }

            if (isElegivel && dados.getNmRotinaCalculo() != null) {
                BigDecimal valor;
                if (dados.getQtdIndiceContribuicao() != null && dados.getQtdIndiceContribuicao().compareTo(BigDecimal.ZERO) > 0) {
                    valor = dados.getQtdIndiceContribuicao();
                } else {
                    valor = rotinaCalculoStrategyFactory
                            .getStrategy(dados.getNmRotinaCalculo())
                            .executar(faturamentos, dados);
                }

                if (valor.compareTo(BigDecimal.ZERO) > 0) {

                    faturamentos.addAll(distribuirInvestimento(distribuicoes, item, dados, valor));

                    System.out.println("Pessoa " + dados.getIdPessoa() + " - " + dados.getNmContribuicao() + ": " + valor);
                }
            }
        }

        item.setMovimentos(faturamentos);
        return item;
    }

    private List<Distribuicao> listarDistribuicao(PlanoProcessamento item, LocalDate dataCompetencia) {
        return distribuicaoRepository.listarDistribuicoes(item.getIdPessoaJuridica(), item.getIdPlano(), dataCompetencia);
    }

    private List<ArrecadacaoMovimento> distribuirInvestimento(
            List<Distribuicao> distribuicoes,
            PlanoProcessamento item,
            DadosParticipante dados,
            BigDecimal valorContribuicao) {

        List<ArrecadacaoMovimento> faturamentos = new ArrayList<>();
        List<Distribuicao> distribuicoesPessoa = distribuicoes.stream()
                .filter(d -> d.getIdContribuicao().equals(dados.getIdContribuicao())
                        && d.getIdPessoaPlano().equals(dados.getIdPessoaPlano())).toList();

        if ("S".equals(dados.getDistribuicaoFlag()) && !distribuicoesPessoa.isEmpty()) {
            for (var distribuicao : distribuicoesPessoa) {
                ArrecadacaoMovimento arrecadacao = new ArrecadacaoMovimento(dados);
                arrecadacao.setValorCalculado(NumberUtils.multiplicarValor(valorContribuicao,
                        NumberUtils.percentualParaTaxa(distribuicao.getPercentualDistribuicao())));
                arrecadacao.setAnoReferencia(item.getAnoReferencia());
                arrecadacao.setMesReferencia(item.getMesReferencia());
                arrecadacao.setIdPerfilInvestimento(distribuicao.getIdPerfilInvestimento());
                faturamentos.add(arrecadacao);
            }
        } else {
            ArrecadacaoMovimento arrecadacao = new ArrecadacaoMovimento(dados);
            arrecadacao.setValorCalculado(valorContribuicao);
            arrecadacao.setAnoReferencia(item.getAnoReferencia());
            arrecadacao.setMesReferencia(item.getMesReferencia());
            faturamentos.add(arrecadacao);
        }

        return faturamentos;
    }

}
