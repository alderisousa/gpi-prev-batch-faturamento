package br.com.gpi.faturamento.data.mapper;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ArrecadacaoMovimentoRowMapper implements RowMapper<ArrecadacaoMovimento> {

    @Override
    public ArrecadacaoMovimento mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ArrecadacaoMovimento.builder()
                .id(rs.getLong("ID"))
                .idContribuicao(rs.getLong("CONTRIBUICAO_ID"))
                .idPerfilInvestimento(rs.getLong("PERFIL_INVESTIMENTO_ID"))
                .idPessoaPlano(rs.getLong("PESSOA_PLANO_ID"))
                .idArrecadacao(rs.getLong("ARRECADACAO_ID"))
                .idIndiceArrecadacao(rs.getLong("INDICE_ARRECADACAO_ID"))
                .idIndiceRecebido(rs.getLong("INDICE_RECEBIDO_ID"))
                .idPerfilInvestimento(rs.getLong("PERFIL_INVESTIMENTO_ID"))
                .anoReferencia(rs.getString("ANO_REFERENCIA"))
                .mesReferencia(rs.getString("MES_REFERENCIA"))
                .valorCalculado(rs.getBigDecimal("VALOR_CALCULADO"))
                .valorRecebido(rs.getBigDecimal("VALOR_RECEBIDO"))
                .valorFinal(rs.getBigDecimal("VALOR_FINAL"))
                .build();
    }
}
