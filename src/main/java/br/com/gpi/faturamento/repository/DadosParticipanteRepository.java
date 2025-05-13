package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.data.mapper.DadosParticipanteRowMapper;
import br.com.gpi.faturamento.domain.DadosParticipante;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DadosParticipanteRepository extends CrudRepository<DadosParticipante, Long> {

    @Query(value = """
           select con.DESCRICAO CONTRIBUICAO_DESCRICAO,
                  con.ID CONTRIBUICAO_ID,
                  pp.DATA_OPCAO,
                  pp.PESSOA_ID,
                  vin.SALARIO,
                  vin.DATA_ADMISSAO,
                  ur.VALOR_UR,
                  con.TIPO_CONTRIBUICAO_ID,
                  con.ATRIBUICAO_PAGAMENTO,
                  pgc.ROTINA_ELEGIBILIDADE_ID,
                  rce.DESCRICAO ROTINA_ELEGIBILIDADE_DESCRICAO,
                  pgc.PARAMETRO_ELEGIBILIDADE,
                  pgc.ROTINA_CALCULO_ID,
                  rcc.DESCRICAO ROTINA_CALCULO_DESCRICAO,
                  pgc.PARAMETRO_CALCULO,
                  pgc.ROTINA_RETORNO_ID,
                  rcr.DESCRICAO ROTINA_RETORNO_DESCRICAO,
                  pgc.PARAMETRO_RETORNO,
                  pc.QTD_INDICE,
                  pc.PERCENTUAL,
                  pc.INDICE_ID,
                  pes.DATA_NASCIMENTO,
                  ppj.DISTRIBUICAO_FLAG,
                  con.CONTRIBUICAO_FLAG,
                  pc.PESSOA_PLANO_ID,
                  pc.ID PESSOA_CONTRIBUICAO_ID
              from CONTRIBUICAO             con
              join PLANO_GRUPO_CONTRIBUICAO pgc on con.ID              = pgc.CONTRIBUICAO_ID
              join PESSOA_PLANO_GRUPO       ppg on ppg.PLANO_GRUPO_ID  = pgc.PLANO_GRUPO_ID
              join PLANO_GRUPO              pg  on pg.ID               = ppg.PLANO_GRUPO_ID
              join PESSOA_PLANO             pp  on ppg.PESSOA_PLANO_ID = pp.ID
              join PESSOA_CONTRIBUICAO      pc  on pc.CONTRIBUICAO_ID  = con.ID
                                               and pp.ID               = pc.PESSOA_PLANO_ID
              join PLANO                    pla on pla.ID              = con.PLANO_ID
              join UNIDADE_REFERENCIA       ur  on ur.PLANO_ID         = pla.ID
                                               and (ur.GRUPO_ID        is null or ur.GRUPO_ID = pg.GRUPO_ID)
              join PESSOA                   pes on pes.ID              = pp.PESSOA_ID
              join VINCULO                  vin on vin.PESSOA_ID       = pp.PESSOA_ID
                                            and vin.DATA_DEMISSAO      is null
              JOIN PLANO_PESSOA_JURIDICA    ppj on ppj.PLANO_ID        = pla.ID
              left join ROTINA_CALCULO      rce on rce.ID              = pgc.ROTINA_ELEGIBILIDADE_ID
              left join ROTINA_CALCULO      rcc on rcc.ID              = pgc.ROTINA_CALCULO_ID
              left join ROTINA_CALCULO      rcr on rcr.ID              = pgc.ROTINA_RETORNO_ID
             where 1=1
               and ppj.PESSOA_JURIDICA_ID = :idPessoaJuridica
               and (:idPlano is null or con.PLANO_ID = :idPlano)
             order by pp.PESSOA_ID, con.ORDEM_CALCULO
           """, rowMapperClass = DadosParticipanteRowMapper.class)
    List<DadosParticipante> listarParticipantes(@Param("idPessoaJuridica") Long idPessoaJuridica, @Param("idPlano") Long idPlano);

}
