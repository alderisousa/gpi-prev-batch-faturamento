package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.data.mapper.DadosParticipanteRowMapper;
import br.com.gpi.faturamento.domain.DadosParticipante;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DadosParticipanteRepository extends CrudRepository<DadosParticipante, Long> {

    @Query(value = """
            select *
              from (select distinct
                           con.DESCRICAO CONTRIBUICAO_DESCRICAO,
                           con.ID CONTRIBUICAO_ID,
                           pp.DATA_OPCAO,
                           pp.PESSOA_ID,
                           vin.ID VINCULO_ID,
                           vin.SALARIO,
                           vin.DATA_ADMISSAO,
                           vin.SITUACAO_VINCULO_ID,
                           ur.VALOR_UR_INICIAL VALOR_UR,
                           con.TIPO_CONTRIBUICAO_ID,
                           pgc.ROTINA_ELEGIBILIDADE_ID,
                           rce.DESCRICAO ROTINA_ELEGIBILIDADE_DESCRICAO,
                           pgc.PARAMETRO_ELEGIBILIDADE,
                           pgc.ROTINA_CALCULO_ID,
                           rcc.DESCRICAO ROTINA_CALCULO_DESCRICAO,
                           pgc.PARAMETRO_CALCULO,
                           pgc.ROTINA_RETORNO_ID,
                           rcr.DESCRICAO ROTINA_RETORNO_DESCRICAO,
                           pgc.PARAMETRO_RETORNO,
                           isnull(pc.QTD_INDICE, pgc.QTD_INDICE) QTD_INDICE,
                           isnull(pc.PERCENTUAL, pgc.PERCENTUAL) PERCENTUAL,
                           isnull(pc.INDICE_ID, pgc.INDICE_ID) INDICE_ID,
                           pes.DATA_NASCIMENTO,
                           ppj.DISTRIBUICAO_FLAG,
                           pp.ID PESSOA_PLANO_ID,
                           pc.ID PESSOA_CONTRIBUICAO_ID,
                           con.ATRIBUICAO_PAGAMENTO_ID,
                           con.ORDEM,
                           pp.PERFIL_INVESTIMENTO_ID,
                           con.FL_SALARIO_PARTICIPACAO SALARIO_PARTICIPACAO_FLAG
                      from CONTRIBUICAO             con
                      join PLANO_GRUPO_CONTRIBUICAO pgc on con.ID              = pgc.CONTRIBUICAO_ID
                      join PESSOA_PLANO_GRUPO       ppg on ppg.PLANO_GRUPO_ID  = pgc.PLANO_GRUPO_ID
                      join PLANO_GRUPO              pg  on pg.ID               = ppg.PLANO_GRUPO_ID
                      join PESSOA_PLANO             pp  on ppg.PESSOA_PLANO_ID = pp.ID
                                                       and pp.SITUACAO_PESSOA_PLANO_ID = 1
                                                       and (con.OPCAO_MODALIDADE_ID = pp.OPCAO_MODALIDADE_ID or con.ATRIBUICAO_PAGAMENTO_ID = 2)
                      join PLANO                    pla on pla.ID              = con.PLANO_ID
                      join PESSOA                   pes on pes.ID              = pp.PESSOA_ID
                      join VINCULO                  vin on vin.PESSOA_ID       = pp.PESSOA_ID
                                                    and (vin.DATA_DEMISSAO     is null or vin.DATA_DEMISSAO > :dataCompetencia)
                      JOIN PLANO_PESSOA_JURIDICA    ppj on ppj.PLANO_ID        = pla.ID
                                                    and ppj.PESSOA_JURIDICA_ID = vin.PJ_ID
                      left join PESSOA_CONTRIBUICAO pc  on pc.CONTRIBUICAO_ID  = con.ID
                                                       and pp.ID               = pc.PESSOA_PLANO_ID
                                                       and :dataCompetencia between pc.DATA_INICIAL and ISNULL(pc.DATA_FINAL, :dataCompetencia)
                      left join UNIDADE_REFERENCIA   ur on ur.PLANO_ID         = pla.ID
                                                       and (ur.PLANO_GRUPO_ID  is null or ur.PLANO_GRUPO_ID = pgc.PLANO_GRUPO_ID)
                      left join ROTINA_CALCULO      rce on rce.ID              = pgc.ROTINA_ELEGIBILIDADE_ID
                      left join ROTINA_CALCULO      rcc on rcc.ID              = pgc.ROTINA_CALCULO_ID
                      left join ROTINA_CALCULO      rcr on rcr.ID              = pgc.ROTINA_RETORNO_ID
                     where 1=1
                       and ppj.PESSOA_JURIDICA_ID = :idPessoaJuridica
                       and (:idPlano is null or con.PLANO_ID = :idPlano)
                       and :dataCompetencia between con.DATA_INICIAL  and ISNULL(con.DATA_FINAL, :dataCompetencia)
                   ) vw
             order by vw.PESSOA_ID, vw.ATRIBUICAO_PAGAMENTO_ID, case when vw.ORDEM is null then 9999 else vw.ORDEM end
           """, rowMapperClass = DadosParticipanteRowMapper.class)
    List<DadosParticipante> listarParticipantes(@Param("idPessoaJuridica") Long idPessoaJuridica,
                                                @Param("idPlano") Long idPlano,
                                                @Param("dataCompetencia") LocalDate dataCompetencia);

}
