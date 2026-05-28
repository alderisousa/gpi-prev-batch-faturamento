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
              from ARRECADACAO_CARGA ac
              join PESSOA_PLANO pp on pp.ID = ac.PESSOA_PLANO_ID
              join VINCULO v on v.PESSOA_ID = pp.PESSOA_ID
                            and v.PJ_ID = :idPessoaJuridica
                            and v.SITUACAO_VINCULO_ID not in (3,8,9)
             where ac.ANO_REFERENCIA = :anoReferencia
               and ac.MES_REFERENCIA = :mesReferencia
            """, rowMapperClass = ArrecadacaoCargaRowMapper.class)
    List<ArrecadacaoCarga> listar(@Param("idPessoaJuridica") Long idPessoaJuridica,
                                  @Param("mesReferencia") String mesReferencia,
                                  @Param("anoReferencia") String anoReferencia);
}
