package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.data.mapper.ArrecadacaoRowMapper;
import br.com.gpi.faturamento.domain.Arrecadacao;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArrecadacaoRepository extends CrudRepository<Arrecadacao, Long> {


    @Query(value = """
            select *
            from ARRECADACAO
            where ID in (select d.ID
                           from CONTRIBUICAO b
                           join PLANO_PESSOA_JURIDICA c on b.PLANO_ID = c.PLANO_ID
                           join ARRECADACAO d on d.CONTRIBUICAO_ID = b.ID
                          where d.ANO_COMPETENCIA = :anoReferencia
                            and d.MES_COMPETENCIA = :mesReferencia
                            and c.PESSOA_JURIDICA_ID = :idPessoaJuridica)
            """, rowMapperClass = ArrecadacaoRowMapper.class)
    List<Arrecadacao> listar(@Param("idPessoaJuridica") Long idPessoaJuridica,
                             @Param("mesReferencia") String mesReferencia,
                             @Param("anoReferencia") String anoReferencia);

    @Modifying
    @Query(value = """
            update a
               set a.VALOR_CALCULADO = vw.VALOR_CALCULADO,
                   a.VALOR_RECEBIDO  = vw.VALOR_RECEBIDO,
                   a.VALOR_FINAL     = vw.VALOR_FINAL
              from ARRECADACAO a
              join (select am.ARRECADACAO_ID,
            		       sum(am.VALOR_CALCULADO) VALOR_CALCULADO,
            		       sum(am.VALOR_RECEBIDO) VALOR_RECEBIDO,
            		       sum(am.VALOR_FINAL) VALOR_FINAL
            		   from CONTRIBUICAO b
            		   join ARRECADACAO_MOVIMENTO am on am.CONTRIBUICAO_ID = b.ID
            		                                and am.ANO_REFERENCIA  = :anoReferencia
            				    					and am.MES_REFERENCIA  = :mesReferencia
            		  where b.PLANO_ID         = :idPlano
            		  group by am.ARRECADACAO_ID) vw on a.ID = vw.ARRECADACAO_ID
            """)
    void atualizarValores(@Param("idPlano") Long idPlano,
                          @Param("mesReferencia") String mesReferencia,
                          @Param("anoReferencia") String anoReferencia);

    @Modifying
    @Query(value = """
            delete
            from ARRECADACAO
            where ID in (select d.ID
                           from CONTRIBUICAO b
                           join PLANO_PESSOA_JURIDICA c on b.PLANO_ID = c.PLANO_ID
                           join ARRECADACAO d on d.CONTRIBUICAO_ID = b.ID
                          where d.ANO_COMPETENCIA = :anoReferencia
                            and d.MES_COMPETENCIA = :mesReferencia
                            and c.PESSOA_JURIDICA_ID = :idPessoaJuridica)
            """, rowMapperClass = ArrecadacaoRowMapper.class)
    void removerPorAnoMesReferencia(Long idPessoaJuridica, String mesReferencia, String anoReferencia);
}
