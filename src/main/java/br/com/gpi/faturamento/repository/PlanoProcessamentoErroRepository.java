package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.domain.PlanoProcessamentoErro;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanoProcessamentoErroRepository extends CrudRepository<PlanoProcessamentoErro, Long> {

}
