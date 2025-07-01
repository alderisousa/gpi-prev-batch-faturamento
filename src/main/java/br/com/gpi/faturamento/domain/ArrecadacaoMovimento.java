package br.com.gpi.faturamento.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Table("ARRECADACAO_MOVIMENTO")
@Builder
@AllArgsConstructor
public class ArrecadacaoMovimento extends ArrecadacaoMovimentoKey {

    @Id
    @Column("ID")
    private Long id;
    @Column("CONTRIBUICAO_ID")
    private Long idContribuicao;
    @Column("PESSOA_PLANO_ID")
    private Long idPessoaPlano;
    @Column("ARRECADACAO_ID")
    private Long idArrecadacao;
    @Column("INDICE_ARRECADACAO_ID")
    private Long idIndiceArrecadacao;
    @Column("INDICE_RECEBIDO_ID")
    private Long idIndiceRecebido;
    @Column("PERFIL_INVESTIMENTO_ID")
    private Long idPerfilInvestimento;
    @Column("ANO_REFERENCIA")
    private String anoReferencia;
    @Column("MES_REFERENCIA")
    private String mesReferencia;
    @Column("VALOR_CALCULADO")
    private BigDecimal valorCalculado;
    @Column("VALOR_RECEBIDO")
    private BigDecimal valorRecebido;
    @Column("VALOR_FINAL")
    private BigDecimal valorFinal;

    @Transient
    private Long idTipoContribuicao;

    @Transient
    private String contribuicaoFlag;

    public ArrecadacaoMovimento(DadosParticipante dados) {
        this.idContribuicao = dados.getIdContribuicao();
        this.idPessoaPlano = dados.getIdPessoaPlano();
        this.idTipoContribuicao = dados.getIdTipoContribuicao();
        this.idIndiceArrecadacao = dados.getIdIndice();
        this.idIndiceRecebido = dados.getIdIndice();
        this.contribuicaoFlag = dados.getContribuicaoFlag();
        this.valorCalculado = BigDecimal.ZERO;
        this.valorRecebido = BigDecimal.ZERO;
        this.valorFinal = BigDecimal.ZERO;
    }

    public ArrecadacaoMovimento(ArrecadacaoCarga dados) {
        this.idContribuicao = dados.getIdContribuicao();
        this.idPessoaPlano = dados.getIdPessoaPlano();
        this.idIndiceArrecadacao = dados.getIdIndice();
        this.idIndiceRecebido = dados.getIdIndice();
        this.valorCalculado = BigDecimal.ZERO;
        this.valorRecebido = dados.getValorRecebido();
        this.valorFinal = dados.getValorRecebido();
        this.idPerfilInvestimento = dados.getIdPerfilInvestimento();
        this.anoReferencia = dados.getAnoReferencia();
        this.mesReferencia = dados.getMesReferencia();
    }
}
