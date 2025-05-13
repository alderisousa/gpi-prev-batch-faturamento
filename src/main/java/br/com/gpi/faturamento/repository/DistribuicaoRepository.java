package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.domain.Distribuicao;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistribuicaoRepository extends PagingAndSortingRepository<Distribuicao, Long>, CrudRepository<Distribuicao, Long> {

    List<Distribuicao> findAllByIdPessoaContribuicaoIn(List<Long> idsPessoaContribuicao);
}
