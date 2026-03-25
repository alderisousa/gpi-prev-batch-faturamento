package br.com.gpi.faturamento.batch.processor;

import br.com.gpi.faturamento.domain.*;
import br.com.gpi.faturamento.repository.DadosParticipanteRepository;
import br.com.gpi.faturamento.repository.DistribuicaoRepository;
import br.com.gpi.faturamento.repository.HistoricoSalarialRepository;
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
import java.util.Optional;

@Component
public class FaturamentoProcessor implements ItemProcessor<PlanoProcessamento, PlanoProcessamento> {

    private static final String FLAG_SIM = "S";

    @Autowired
    private DadosParticipanteRepository dadosParticipanteRepository;

    @Autowired
    private RotinaCalculoStrategyFactory rotinaCalculoStrategyFactory;

    @Autowired
    private RotinaElegibilidadeStrategyFactory rotinaElegibilidadeFactoryStrategy;

    @Autowired
    private DistribuicaoRepository distribuicaoRepository;

    @Autowired
    private HistoricoSalarialRepository historicoSalarialRepository;

    @Override
    public PlanoProcessamento process(PlanoProcessamento item) {

        LocalDate dataCompetencia = DateUtils.getCompetencia(item.getAnoReferencia(), item.getMesReferencia());
        LocalDate dataCompetenciaSalario = DateUtils.getCompetenciaSalario(item.getAnoReferencia(), item.getMesReferencia());

        var dadosParticipantes = dadosParticipanteRepository.listarParticipantes(item.getIdPessoaJuridica(), item.getIdPlano(), dataCompetencia);

        var salarios = historicoSalarialRepository.listarSalarios(item.getIdPessoaJuridica(), dataCompetenciaSalario);

        List<Distribuicao> distribuicoes = listarDistribuicao(item, dataCompetencia);

        List<ArrecadacaoMovimento> faturamentos = new ArrayList<>();
        for (DadosParticipante dados : dadosParticipantes) {

            atualizarSalario(dados, salarios);

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

                    var valorCom13 = calcular13(valor, item.getMesReferencia(), dados);

                    faturamentos.addAll(distribuirInvestimento(distribuicoes, item, dados, valorCom13));

                    System.out.println("Pessoa " + dados.getIdPessoa() + " - " + dados.getNmContribuicao() + ": " + valorCom13);
                }
            }
        }

        item.setMovimentos(faturamentos);
        return item;
    }

    private BigDecimal calcular13(BigDecimal valor, String mesReferencia, DadosParticipante dados) {

        if (mesReferencia.equals(dados.getMes13())
                && dados.getPct13() != null
                && FLAG_SIM.equals(dados.getIncide13Flag())
                && FLAG_SIM.equals(dados.getDobra13Flag())) {
            var valor13 = NumberUtils.multiplicarValor(valor, NumberUtils.percentualParaTaxa(dados.getPct13()));
            return valor.add(valor13);
        }

        return valor;
    }

    private void atualizarSalario(DadosParticipante dados, List<HistoricoSalarial> salarios) {
        Optional<HistoricoSalarial> optSalario = salarios.stream().filter(s -> s.getIdVinculo().equals(dados.getIdVinculo())).findFirst();

        optSalario.ifPresent(historicoSalarial -> {
            BigDecimal salario;
            if (FLAG_SIM.equals(dados.getContrib13Flag()) && historicoSalarial.getVrSalario13() != null)
                salario = historicoSalarial.getVrSalario13();
            else if (FLAG_SIM.equals(dados.getSalarioParticipacaoFlag()))
                salario = historicoSalarial.getVrSalarioParticipacao();
            else
                salario = historicoSalarial.getVrSalario();

            dados.setSalarioContribuicao(salario);
        });
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
                arrecadacao.setValorFinal(dados.getSomaValorRecebido());
                faturamentos.add(arrecadacao);
            }
        } else {
            ArrecadacaoMovimento arrecadacao = new ArrecadacaoMovimento(dados);
            arrecadacao.setValorCalculado(valorContribuicao);
            arrecadacao.setAnoReferencia(item.getAnoReferencia());
            arrecadacao.setMesReferencia(item.getMesReferencia());
            arrecadacao.setIdPerfilInvestimento(dados.getIdPerfilInvestimento());
            arrecadacao.setValorFinal(dados.getSomaValorRecebido());
            faturamentos.add(arrecadacao);
        }

        return faturamentos;
    }

}
