package br.com.gpi.faturamento.strategy.elegibilidade;

import br.com.gpi.faturamento.domain.DadosParticipante;

public interface RotinaElegibilidadeStrategy {

    boolean executar(DadosParticipante dados);

    boolean executar(DadosParticipante dados, String parametros);

}
