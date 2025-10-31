package br.com.gpi.faturamento.strategy.calculo;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.strategy.calculo.vo.ParametroCalculoPorFaixaVO;
import br.com.gpi.faturamento.utils.NumberUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component("FAIXA")
public class CalcularPorFaixaPercentualStrategy implements RotinaCalculoStrategy {

    @Override
    public BigDecimal executar(List<ArrecadacaoMovimento> movimentos, DadosParticipante dados) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ParametroCalculoPorFaixaVO parametro = mapper.readValue(dados.getJsonParametroCalculo(), ParametroCalculoPorFaixaVO.class);
            BigDecimal valorCalculo = getValorCalculo(dados, parametro);

            BigDecimal pctContribuicao = getPercentualContribuicao(dados, parametro);

            BigDecimal resultadoCalculo = NumberUtils.max(BigDecimal.ZERO,
                    NumberUtils.multiplicarValor(valorCalculo,
                            NumberUtils.percentualParaTaxa(pctContribuicao),
                            parametro.getQtDecimais()));
            return parametro.getVlMinimoContribuicao() != null
                    && parametro.getVlMinimoContribuicao().compareTo(resultadoCalculo) > 0
                    && pctContribuicao != null && BigDecimal.ZERO.compareTo(pctContribuicao) < 0
                        ? parametro.getVlMinimoContribuicao()
                        : resultadoCalculo;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    private BigDecimal getPercentualContribuicao(DadosParticipante dados, ParametroCalculoPorFaixaVO parametro) {
        if (parametro.getPctContribuicaoMax() != null && parametro.getPctContribuicaoMax().compareTo(dados.getPctContribuicao()) < 0) {
            return parametro.getPctContribuicaoMax();
        }
        return dados.getPctContribuicao();
    }

    private BigDecimal getValorCalculo(DadosParticipante dados, ParametroCalculoPorFaixaVO parametro) {
        BigDecimal valorMinimo = getValorMinimo(dados, parametro);
        BigDecimal valorMaximo = getValorMaximo(dados, parametro).subtract(valorMinimo);

        BigDecimal valorRestante = dados.getSalarioContribuicao().subtract(valorMinimo);

        return NumberUtils.min(valorMaximo, valorRestante);
    }

    private static BigDecimal getValorMaximo(DadosParticipante dados, ParametroCalculoPorFaixaVO parametro) {
        BigDecimal valorMaximo;
        if (parametro.getVlFaixaMax() != null) {
            valorMaximo = parametro.getVlFaixaMax();
        } else {
            valorMaximo = NumberUtils.multiplicarValor(parametro.getQtdURMax(), dados.getValorUR(), parametro.getQtDecimais());
        }
        return valorMaximo;
    }

    private static BigDecimal getValorMinimo(DadosParticipante dados, ParametroCalculoPorFaixaVO parametro) {
        BigDecimal valorMinimo;
        if (parametro.getVlFaixaMin() != null) {
            valorMinimo = parametro.getVlFaixaMin();
        } else {
            valorMinimo = NumberUtils.multiplicarValor(parametro.getQtdURMin(), dados.getValorUR(), parametro.getQtDecimais());
        }
        return valorMinimo;
    }
}
