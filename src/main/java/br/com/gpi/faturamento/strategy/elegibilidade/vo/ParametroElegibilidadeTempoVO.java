package br.com.gpi.faturamento.strategy.elegibilidade.vo;

import lombok.Data;

@Data
public class ParametroElegibilidadeTempoVO {
    private Long idadeMinima;
    private Long idadeMaxima;
    private Long tempoEmpresaMinimo;
    private Long tempoEmpresaMaximo;
    private Long tempoPlanoMinimo;
    private Long tempoPlanoMaximo;
}
