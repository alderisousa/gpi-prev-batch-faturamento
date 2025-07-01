package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.data.mapper.ArrecadacaoCargaRowMapper;
import br.com.gpi.faturamento.domain.ArrecadacaoCarga;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArrecadacaoCargaRepository extends CrudRepository<ArrecadacaoCarga, Long> {

    @Query(value = """
            select *
              from ARRECADACAO_CARGA
             where CONTRIBUICAO_ID in (select b.ID
                                         from CONTRIBUICAO b
                                         join PLANO_PESSOA_JURIDICA c on b.PLANO_ID = c.PLANO_ID
                                        where c.PESSOA_JURIDICA_ID = :idPessoaJuridica)
               and ANO_REFERENCIA = :anoReferencia
               and MES_REFERENCIA = :mesReferencia
            """, rowMapperClass = ArrecadacaoCargaRowMapper.class)
    List<ArrecadacaoCarga> listar(@Param("idPessoaJuridica") Long idPessoaJuridica,
                                  @Param("mesReferencia") String mesReferencia,
                                  @Param("anoReferencia") String anoReferencia);
}
