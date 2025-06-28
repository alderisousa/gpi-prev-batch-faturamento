package br.com.gpi.faturamento.data.mapper;

import br.com.gpi.faturamento.domain.Arrecadacao;
import br.com.gpi.faturamento.domain.DadosParticipante;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public class ArrecadacaoRowMapper implements RowMapper<Arrecadacao> {

    @Override
    public Arrecadacao mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Arrecadacao.builder()
                .id(rs.getLong("ID"))
                .idContribuicao(rs.getLong("CONTRIBUICAO_ID"))
                .idPerfilInvestimento(Optional.ofNullable(rs.getString("PERFIL_INVESTIMENTO_ID")).map(Long::parseLong).orElse(null))
                .idIndice(rs.getLong("INDICE_ID"))
                .anoCompetencia(rs.getString("ANO_COMPETENCIA"))
                .mesCompetencia(rs.getString("MES_COMPETENCIA"))
                .dataVencimento(Optional.ofNullable(rs.getDate("DATA_VENCIMENTO")).map(Date::toLocalDate).orElse(null))
                .dataPagamento(Optional.ofNullable(rs.getDate("DATA_PAGAMENTO")).map(Date::toLocalDate).orElse(null))
                .valorCalculado(BigDecimal.ZERO)
                .valorRecebido(rs.getBigDecimal("VALOR_RECEBIDO"))
                .valorFinal(rs.getBigDecimal("VALOR_FINAL"))
                .idIndice(rs.getLong("INDICE_ID"))
                .movimentos(new ArrayList<>())
                .build();
    }
}
