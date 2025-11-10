package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimentoHistorico;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArrecadacaoMovimentoHistoricoRepository extends CrudRepository<ArrecadacaoMovimentoHistorico, Long> {

}
