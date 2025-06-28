package br.com.gpi.faturamento.data.mapper;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ArrecadacaoMovimentoRowMapper implements RowMapper<ArrecadacaoMovimento> {

    @Override
    public ArrecadacaoMovimento mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ArrecadacaoMovimento.builder()
                .id(rs.getLong("ID"))
                .idContribuicao(rs.getLong("CONTRIBUICAO_ID"))
                .idPerfilInvestimento(Optional.ofNullable(rs.getString("PERFIL_INVESTIMENTO_ID")).map(Long::parseLong).orElse(null))
                .idPessoaPlano(rs.getLong("PESSOA_PLANO_ID"))
                .idArrecadacao(rs.getLong("ARRECADACAO_ID"))
                .idIndiceArrecadacao(rs.getLong("INDICE_ARRECADACAO_ID"))
                .idIndiceRecebido(rs.getLong("INDICE_RECEBIDO_ID"))
                .anoReferencia(rs.getString("ANO_REFERENCIA"))
                .mesReferencia(rs.getString("MES_REFERENCIA"))
                .valorCalculado(BigDecimal.ZERO)
                .valorRecebido(rs.getBigDecimal("VALOR_RECEBIDO"))
                .valorFinal(rs.getBigDecimal("VALOR_FINAL"))
                .build();
    }

}
