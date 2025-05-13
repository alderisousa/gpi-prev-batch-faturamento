package br.com.gpi.faturamento.strategy.calculo;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.strategy.calculo.vo.ParametroCalculoMultiplicacaoVO;
import br.com.gpi.faturamento.strategy.calculo.vo.ParametroCalculoPorFaixaVO;
import br.com.gpi.faturamento.utils.NumberUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component("MULTIPLICA_PERCENTUAL")
public class MultiplicaPercentualStrategy implements RotinaCalculoStrategy {

    @Override
    public BigDecimal executar(List<ArrecadacaoMovimento> movimentos, DadosParticipante dados) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ParametroCalculoMultiplicacaoVO parametro = mapper.readValue(dados.getJsonParametroCalculo(), ParametroCalculoMultiplicacaoVO.class);
            BigDecimal valorCalculo = getValorCalculo(dados, parametro);

            return NumberUtils.max(BigDecimal.ZERO,
                    NumberUtils.multiplicarValor(valorCalculo,
                            NumberUtils.percentualParaFaixa(dados.getPctContribuicao())));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    private BigDecimal getValorCalculo(DadosParticipante dados, ParametroCalculoMultiplicacaoVO parametro) {
        return NumberUtils.multiplicarValor(parametro.getQtdUR(), dados.getValorUR());
    }

}
