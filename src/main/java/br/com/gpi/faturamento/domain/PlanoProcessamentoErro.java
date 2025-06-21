package br.com.gpi.faturamento.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("PLANO_PROCESSAMENTO_ERRO")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanoProcessamentoErro {

    @Id
    @Column("ID")
    private Long id;
    @Column("PLANO_PROCESSAMENTO_ID")
    private Long idPlanoProcessamento;
    @Column("TP_EXCECAO")
    private String tipoExcecao;
    @Column("DS_EXCECAO")
    private String descricao;
    @Column("DS_STACK")
    private String stack;
    @Column("TP_EXCECAO_CAUSA")
    private String tipoExcecaoCausa;
    @Column("DS_EXCECAO_CAUSA")
    private String descricaoCausa;
    @Column("DS_STACK_CAUSA")
    private String stackCausa;
}
