package br.com.gpi.faturamento.strategy.calculo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametroCalculoPorFaixaVO {

    private BigDecimal qtdURMin;
    private BigDecimal qtdURMax;
    private BigDecimal vlFaixaMin;
    private BigDecimal vlFaixaMax;
    private BigDecimal pctContribuicaoMax;
    private BigDecimal vlMinimoContribuicao;
    private Integer qtDecimais;

}

