package br.com.gpi.faturamento.strategy.calculo;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import br.com.gpi.faturamento.domain.DadosParticipante;

import java.math.BigDecimal;
import java.util.List;

public interface RotinaCalculoStrategy {

    BigDecimal executar(List<ArrecadacaoMovimento> movimentos, DadosParticipante dados);

}
