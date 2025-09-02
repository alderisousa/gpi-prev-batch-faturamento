package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.data.mapper.ArrecadacaoMovimentoRowMapper;
import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArrecadacaoMovimentoRepository extends CrudRepository<ArrecadacaoMovimento, Long> {

    @Query(value = """
             select am.*
               from CONTRIBUICAO b
               join PLANO_PESSOA_JURIDICA c on b.PLANO_ID = c.PLANO_ID
               join VINCULO v on v.PJ_ID = c.PESSOA_JURIDICA_ID
               join PESSOA_PLANO pp on pp.PESSOA_ID = v.PESSOA_ID
               join ARRECADACAO d on d.CONTRIBUICAO_ID = b.ID
               join ARRECADACAO_MOVIMENTO am on am.ARRECADACAO_ID = d.ID
                                            and am.PESSOA_PLANO_ID = pp.ID
              where d.ANO_COMPETENCIA = :anoReferencia
                and d.MES_COMPETENCIA = :mesReferencia
                and c.PESSOA_JURIDICA_ID = :idPessoaJuridica
            """, rowMapperClass = ArrecadacaoMovimentoRowMapper.class)
    List<ArrecadacaoMovimento> listar(@Param("idPessoaJuridica") Long idPessoaJuridica,
                                      @Param("mesReferencia") String mesReferencia,
                                      @Param("anoReferencia") String anoReferencia);

    @Modifying
    @Query(value = """
             delete
               from ARRECADACAO_MOVIMENTO
              where ARRECADACAO_ID in (
                 select d.ID
                   from CONTRIBUICAO b
                   join PLANO_PESSOA_JURIDICA c on b.PLANO_ID = c.PLANO_ID
                   join ARRECADACAO d on d.CONTRIBUICAO_ID = b.ID
                  where d.ANO_COMPETENCIA = :anoReferencia
                    and d.MES_COMPETENCIA = :mesReferencia
                    and c.PESSOA_JURIDICA_ID = :idPessoaJuridica)
            """)
    void removerPorAnoMesReferencia(Long idPessoaJuridica, String mesReferencia, String anoReferencia);
}
