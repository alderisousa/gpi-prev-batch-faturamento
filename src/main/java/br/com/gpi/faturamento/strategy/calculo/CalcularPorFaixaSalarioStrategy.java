package br.com.gpi.faturamento.strategy.calculo;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.strategy.calculo.vo.FaixaTempoPlanoVO;
import br.com.gpi.faturamento.strategy.calculo.vo.ParametroFaixaSalarioVO;
import br.com.gpi.faturamento.utils.DateUtils;
import br.com.gpi.faturamento.utils.NumberUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component("FAIXA_SALARIO")
public class CalcularPorFaixaSalarioStrategy implements RotinaCalculoStrategy {

    @Override
    public BigDecimal executar(List<ArrecadacaoMovimento> movimentos, DadosParticipante dados) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            ParametroFaixaSalarioVO parametro = mapper.readValue(dados.getJsonParametroCalculo(), ParametroFaixaSalarioVO.class);

            BigDecimal percentual = getPercentual(dados, parametro);
            return NumberUtils.multiplicarValor(dados.getSalarioContribuicao(), percentual, parametro.getQtDecimais());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }


    private BigDecimal getPercentual(DadosParticipante dados, ParametroFaixaSalarioVO parametro) {
        if (parametro.getFaixas() != null && !parametro.getFaixas().isEmpty()) {
            return NumberUtils.percentualParaTaxa(getPercentualPorFaixa(dados, parametro.getFaixas()));
        }
        return NumberUtils.percentualParaTaxa(dados.getPctContribuicao());
    }

    private BigDecimal getPercentualPorFaixa(DadosParticipante dados, List<FaixaTempoPlanoVO> faixas) {
        long tempoPlano = DateUtils.calculaAnoAteHoje(dados.getDataOpcao());
        for (var faixa: faixas) {
          if (tempoPlano >= faixa.getTempoPlanoMinimo() && tempoPlano < faixa.getTempoPlanoMaximo()) {
              return faixa.getPercentual();
          }
        }
        return BigDecimal.ONE;
    }

}
