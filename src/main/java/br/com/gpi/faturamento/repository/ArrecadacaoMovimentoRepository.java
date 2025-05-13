package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArrecadacaoMovimentoRepository extends CrudRepository<ArrecadacaoMovimento, Long> {

    @Modifying
    @Query("""
            delete
            from ARRECADACAO_MOVIMENTO
            where ARRECADACAO_ID in (select d.ID
                                       from CONTRIBUICAO b
                                       join PLANO_PESSOA_JURIDICA c on b.PLANO_ID = c.PLANO_ID
                                       join ARRECADACAO d on d.CONTRIBUICAO_ID = b.ID
                                      where d.ANO_COMPETENCIA = :anoReferencia
                                        and d.MES_COMPETENCIA = :mesReferencia
                                        and c.PESSOA_JURIDICA_ID = :idPessoaJuridica)
            """)
    void deleteMovimentoPorCompetencia(@Param("idPessoaJuridica") Long idPessoaJuridica,
                                       @Param("mesReferencia") String mesReferencia,
                                       @Param("anoReferencia") String anoReferencia);
}
