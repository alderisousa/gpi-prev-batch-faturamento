package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.domain.PlanoProcessamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanoProcessamentoRepository extends PagingAndSortingRepository<PlanoProcessamento, Long>, CrudRepository<PlanoProcessamento, Long> {

    Page<PlanoProcessamento> findByIdStatus(Long idStatus, Pageable pageable);

}
