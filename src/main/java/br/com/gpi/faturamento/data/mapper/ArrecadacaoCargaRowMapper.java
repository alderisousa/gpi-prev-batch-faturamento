package br.com.gpi.faturamento.data.mapper;

import br.com.gpi.faturamento.domain.ArrecadacaoCarga;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ArrecadacaoCargaRowMapper implements RowMapper<ArrecadacaoCarga> {

    @Override
    public ArrecadacaoCarga mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ArrecadacaoCarga.builder()
                .id(rs.getLong("ID"))
                .idContribuicao(rs.getLong("CONTRIBUICAO_ID"))
                .idPerfilInvestimento(Optional.ofNullable(rs.getString("PERFIL_INVESTIMENTO_ID")).map(Long::parseLong).orElse(null))
                .idPessoaPlano(rs.getLong("PESSOA_PLANO_ID"))
                .idIndice(rs.getLong("INDICE_ID"))
                .anoReferencia(rs.getString("ANO_REFERENCIA"))
                .mesReferencia(rs.getString("MES_REFERENCIA"))
                .valorRecebido(rs.getBigDecimal("VALOR_RECEBIDO"))
                .idPessoaJuridica(rs.getLong("PJ_ID"))
                .build();
    }

}
