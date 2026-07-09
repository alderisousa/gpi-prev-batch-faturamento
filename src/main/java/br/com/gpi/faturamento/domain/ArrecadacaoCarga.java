package br.com.gpi.faturamento.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Table("ARRECADACAO_CARGA")
@Builder
@AllArgsConstructor
public class ArrecadacaoCarga extends ArrecadacaoMovimentoKey {

    @Id
    @Column("ID")
    private Long id;
    @Column("CONTRIBUICAO_ID")
    private Long idContribuicao;
    @Column("PESSOA_PLANO_ID")
    private Long idPessoaPlano;
    @Column("PJ_ID")
    private Long idPessoaJuridica;
    @Column("INDICE_ID")
    private Long idIndice;
    @Column("PERFIL_INVESTIMENTO_ID")
    private Long idPerfilInvestimento;
    @Column("ANO_REFERENCIA")
    private String anoReferencia;
    @Column("MES_REFERENCIA")
    private String mesReferencia;
    @Column("VALOR_RECEBIDO")
    private BigDecimal valorRecebido;

}