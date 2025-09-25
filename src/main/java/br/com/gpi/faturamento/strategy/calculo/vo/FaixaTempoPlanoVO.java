package br.com.gpi.faturamento.strategy.calculo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaixaTempoPlanoVO {
    private Long tempoPlanoMinimo;
    private Long tempoPlanoMaximo;
    private BigDecimal percentual;
}