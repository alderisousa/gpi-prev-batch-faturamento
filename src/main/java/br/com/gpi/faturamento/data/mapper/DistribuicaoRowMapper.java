package br.com.gpi.faturamento.data.mapper;

import br.com.gpi.faturamento.domain.Distribuicao;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DistribuicaoRowMapper implements RowMapper<Distribuicao> {

    @Override
    public Distribuicao mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Distribuicao.builder()
                .id(rs.getLong("ID"))
                .idPessoaPlano(rs.getLong("PESSOA_PLANO_ID"))
                .idPessoaContribuicao(rs.getLong("PESSOA_CONTRIBUICAO_ID"))
                .idPerfilInvestimento(rs.getLong("PERFIL_INVESTIMENTO_ID"))
                .percentualDistribuicao(rs.getBigDecimal("PERCENTUAL_DISTRIBUICAO"))
                .dataInicio(Optional.ofNullable(rs.getDate("DATA_INICIO")).map(Date::toLocalDate).orElse(null))
                .dataFim(Optional.ofNullable(rs.getDate("DATA_FIM")).map(Date::toLocalDate).orElse(null))
                .build();
    }
}
