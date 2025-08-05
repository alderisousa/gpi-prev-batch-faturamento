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
            where PESSOA_CONTRIBUICAO_ID in (
            	select distinct pc.ID PESSOA_CONTRIBUICAO_ID
            	      from CONTRIBUICAO             con
            	      join PLANO_GRUPO_CONTRIBUICAO pgc on con.ID              = pgc.CONTRIBUICAO_ID
            	      join PESSOA_PLANO_GRUPO       ppg on ppg.PLANO_GRUPO_ID  = pgc.PLANO_GRUPO_ID
            	      join PLANO_GRUPO              pg  on pg.ID               = ppg.PLANO_GRUPO_ID
            	      join PESSOA_PLANO             pp  on ppg.PESSOA_PLANO_ID = pp.ID
            	                                       and (con.OPCAO_MODALIDADE_ID = pp.OPCAO_MODALIDADE_ID or con.ATRIBUICAO_PAGAMENTO_ID = 2)
            	      join PLANO                    pla on pla.ID              = con.PLANO_ID
            	      join VINCULO                  vin on vin.PESSOA_ID       = pp.PESSOA_ID
            	                                    and vin.DATA_DEMISSAO      is null
            	      JOIN PLANO_PESSOA_JURIDICA    ppj on ppj.PLANO_ID        = pla.ID
            	                                       and ppj.PESSOA_JURIDICA_ID = vin.PJ_ID
            	      join PESSOA_CONTRIBUICAO pc       on pc.CONTRIBUICAO_ID  = con.ID
            	                                       and pp.ID               = pc.PESSOA_PLANO_ID
            	                                       and :dataCompetencia between pc.DATA_INICIAL and ISNULL(pc.DATA_FINAL, :dataCompetencia)
            	     where 1=1
            	       and ppj.PESSOA_JURIDICA_ID = :idPessoaJuridica
            	       and (:idPlano is null or con.PLANO_ID = :idPlano)
            	       and :dataCompetencia between con.DATA_INICIAL  and ISNULL(con.DATA_FINAL, :dataCompetencia))
            """, rowMapperClass = DistribuicaoRowMapper.class)
    List<Distribuicao> listarDistribuicoes(@Param("idPessoaJuridica") Long idPessoaJuridica,
                                           @Param("idPlano") Long idPlano,
                                           @Param("dataCompetencia") LocalDate dataCompetencia);
}
