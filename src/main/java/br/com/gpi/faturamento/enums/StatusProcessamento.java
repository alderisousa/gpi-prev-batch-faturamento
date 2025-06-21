package br.com.gpi.faturamento.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusProcessamento {

    PENDENTE(1L),
    EM_PROCESSAMENTO(2L),
    PROCESSADO(3L),
    PROCESSADO_ERRO(4L);

    private final Long id;

}
