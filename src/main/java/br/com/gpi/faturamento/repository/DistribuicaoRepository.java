package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.data.mapper.DistribuicaoRowMapper;
import br.com.gpi.faturamento.domain.Distribuicao;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DistribuicaoRepository extends PagingAndSortingRepository<Distribuicao, Long>, CrudRepository<Distribuicao, Long> {

    @Query(value = """
            select *
            from PESSOA_PERFIL_CONTRIBUICAO_DISTRIBUICAO
            where CONTRIBUICAO_ID in (
            	select distinct con.ID
                  from CONTRIBUICAO             con
                  join PLANO_GRUPO_CONTRIBUICAO pgc on con.ID              = pgc.CONTRIBUICAO_ID
                  join PLANO_GRUPO              pg  on pg.ID               = pgc.PLANO_GRUPO_ID
                  JOIN PLANO_PESSOA_JURIDICA    ppj on ppj.PLANO_ID        = pg.PLANO_ID
                 where 1=1
                   and ppj.PESSOA_JURIDICA_ID = :idPessoaJuridica
                   and (:idPlano is null or con.PLANO_ID = :idPlano)
                   and :dataCompetencia between con.DATA_INICIAL  and ISNULL(con.DATA_FINAL, :dataCompetencia))
            """, rowMapperClass = DistribuicaoRowMapper.class)
    List<Distribuicao> listarDistribuicoes(@Param("idPessoaJuridica") Long idPessoaJuridica,
                                           @Param("idPlano") Long idPlano,
                                           @Param("dataCompetencia") LocalDate dataCompetencia);
}
