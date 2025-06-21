package br.com.gpi.faturamento.data.mapper;

import br.com.gpi.faturamento.domain.DadosParticipante;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DadosParticipanteRowMapper implements RowMapper<DadosParticipante> {

    @Override
    public DadosParticipante mapRow(ResultSet rs, int rowNum) throws SQLException {
        return DadosParticipante.builder()
                .dataOpcao(Optional.ofNullable(rs.getDate("DATA_OPCAO")).map(Date::toLocalDate).orElse(null))
                .nmContribuicao(rs.getString("CONTRIBUICAO_DESCRICAO"))
                .idContribuicao(rs.getLong("CONTRIBUICAO_ID"))
                .idPessoa(rs.getLong("PESSOA_ID"))
                .idIndice(rs.getLong("INDICE_ID"))
                .salarioContribuicao(rs.getBigDecimal("SALARIO"))
                .dataAdmissao(Optional.ofNullable(rs.getDate("DATA_ADMISSAO")).map(Date::toLocalDate).orElse(null))
                .valorUR(rs.getBigDecimal("VALOR_UR"))
                .idTipoContribuicao(rs.getLong("TIPO_CONTRIBUICAO_ID"))
                .idRotinaElegibilidade(rs.getLong("ROTINA_ELEGIBILIDADE_ID"))
                .nmRotinaElegibilidade(rs.getString("ROTINA_ELEGIBILIDADE_DESCRICAO"))
                .jsonParametroElegibilidade(rs.getString("PARAMETRO_ELEGIBILIDADE"))
                .idRotinaCalculo(rs.getLong("ROTINA_CALCULO_ID"))
                .nmRotinaCalculo(rs.getString("ROTINA_CALCULO_DESCRICAO"))
                .jsonParametroCalculo(rs.getString("PARAMETRO_CALCULO"))
                .idRotinaRetorno(rs.getLong("ROTINA_RETORNO_ID"))
                .nmRotinaRetorno(rs.getString("ROTINA_RETORNO_DESCRICAO"))
                .jsonParametroRetorno(rs.getString("PARAMETRO_RETORNO"))
                .qtdIndiceContribuicao(rs.getBigDecimal("QTD_INDICE"))
                .pctContribuicao(rs.getBigDecimal("PERCENTUAL"))
                .dataNascimento(Optional.ofNullable(rs.getDate("DATA_NASCIMENTO")).map(Date::toLocalDate).orElse(null))
                .distribuicaoFlag(rs.getString("DISTRIBUICAO_FLAG"))
                .idPessoaPlano(rs.getLong("PESSOA_PLANO_ID"))
                .idPessoaContribuicao(rs.getLong("PESSOA_CONTRIBUICAO_ID"))
                .build();
    }
}
