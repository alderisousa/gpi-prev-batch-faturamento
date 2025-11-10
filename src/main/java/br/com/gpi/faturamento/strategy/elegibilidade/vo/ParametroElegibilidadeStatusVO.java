package br.com.gpi.faturamento.strategy.elegibilidade.vo;

import lombok.Data;

import java.util.List;

@Data
public class ParametroElegibilidadeStatusVO {
    private String metodo;
    private List<Long> ids;
}
