package br.com.gpi.faturamento.repository;

import br.com.gpi.faturamento.data.mapper.HistoricoSalarialRowMapper;
import br.com.gpi.faturamento.domain.HistoricoSalarial;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HistoricoSalarialRepository extends PagingAndSortingRepository<HistoricoSalarial, Long>, CrudRepository<HistoricoSalarial, Long> {

    @Query(value = """
            select hs.*
              from VINCULO v
              join HISTORICO_SALARIAL hs on v.ID = hs.VINCULO_ID
             where v.PJ_ID = :idPessoaJuridica
               and hs.DATA_SALARIO between :dataCompetenciaSalario and :dataCompetencia
            """, rowMapperClass = HistoricoSalarialRowMapper.class)
    List<HistoricoSalarial> listarSalarios(@Param("idPessoaJuridica") Long idPessoaJuridica,
                                           @Param("dataCompetenciaSalario") LocalDate dataCompetenciaSalario,
                                           @Param("dataCompetencia") LocalDate dataCompetencia);
}
