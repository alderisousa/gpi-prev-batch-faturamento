package br.com.gpi.faturamento.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Table("ARRECADACAO")
public class Arrecadacao {

    @Id
    @Column("ID")
    private Long id;
    @Column("CONTRIBUICAO_ID")
    private Long idContribuicao;
    @Column("PERFIL_INVESTIMENTO_ID")
    private Long idPerfilInvestimento;
    @Column("ANO_COMPETENCIA")
    private String anoCompetencia;
    @Column("MES_COMPETENCIA")
    private String mesCompetencia;
    @Column("DATA_VENCIMENTO")
    private LocalDate dataVencimento;
    @Column("DATA_PAGAMENTO")
    private LocalDate dataPagamento;
    @Column("VALOR_NOMINAL")
    private BigDecimal valorNominal;
    @Transient
    private List<ArrecadacaoMovimento> movimentos = new ArrayList<>();

    public Arrecadacao(ArrecadacaoMovimento movimento) {
        this.idContribuicao = movimento.getIdContribuicao();
        this.anoCompetencia = movimento.getAnoReferencia();
        this.mesCompetencia = movimento.getMesReferencia();
        this.dataVencimento = LocalDate.now(); //TODO verificar
        this.dataPagamento = LocalDate.now(); //TODO verificar
        this.valorNominal = movimento.getValorNominal();
        this.idPerfilInvestimento = movimento.getIdPerfilInvestimento();
        this.movimentos.add(movimento);
    }
}
