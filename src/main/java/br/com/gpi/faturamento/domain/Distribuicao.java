package br.com.gpi.faturamento.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Table("PESSOA_PERFIL_CONTRIBUICAO_DISTRIBUICAO")
public class Distribuicao {

    @Id
    @Column("ID")
    private Long id;
    @Column("PESSOA_PLANO_ID")
    private Long idPessoaPlano;
    @Column("PESSOA_CONTRIBUICAO_ID")
    private Long idPessoaContribuicao;
    @Column("PERFIL_INVESTIMENTO_ID")
    private Long idPerfilInvestimento;
    @Column("PERCENTUAL_DISTRIBUICAO")
    private BigDecimal percentualDistribuicao;
    @Column("DATA_INICIO")
    private LocalDate dataInicio;
    @Column("DATA_FIM")
    private LocalDate dataFim;

}
