package br.com.gpi.faturamento.data.mapper;

import br.com.gpi.faturamento.domain.HistoricoSalarial;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class HistoricoSalarialRowMapper implements RowMapper<HistoricoSalarial> {

    @Override
    public HistoricoSalarial mapRow(ResultSet rs, int rowNum) throws SQLException {
        return HistoricoSalarial.builder()
                .id(rs.getLong("ID"))
                .idVinculo(rs.getLong("VINCULO_ID"))
                .dataSalario(Optional.ofNullable(rs.getDate("DATA_SALARIO")).map(Date::toLocalDate).orElse(null))
                .vrSalario(rs.getBigDecimal("SALARIO"))
                .vrSalarioParticipacao(rs.getBigDecimal("SALARIO_PARTICIPACAO"))
                .build();
    }
}
