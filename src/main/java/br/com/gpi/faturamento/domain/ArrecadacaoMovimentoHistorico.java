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
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Table("ARRECADACAO_MOVIMENTO_HISTORICO")
@Builder
@AllArgsConstructor
public class ArrecadacaoMovimentoHistorico {

    @Id
    @Column("ID")
    private Long id;
    @Column("CONTRIBUICAO_ID")
    private Long idContribuicao;
    @Column("PESSOA_PLANO_ID")
    private Long idPessoaPlano;
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
    @Column("DESCRICAO_ALTERACAO")
    private String descricao;
    @Column("DS_LOGIN")
    private String login;
    @Column("DATA_CADASTRO")
    private LocalDateTime dataCadastro;

    public ArrecadacaoMovimentoHistorico(ArrecadacaoMovimento movimento) {
        this.idContribuicao = movimento.getIdContribuicao();
        this.idPessoaPlano = movimento.getIdPessoaPlano();
        this.anoReferencia = movimento.getAnoReferencia();
        this.mesReferencia = movimento.getMesReferencia();
        this.valorCalculado = movimento.getValorCalculado();
        this.valorRecebido = movimento.getValorRecebido();
        this.valorFinal = movimento.getValorFinal();
        this.descricao = "Processamento de cálculo";
        this.dataCadastro = LocalDateTime.now();
    }
}
