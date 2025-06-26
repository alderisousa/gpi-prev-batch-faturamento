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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@Table("ARRECADACAO")
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    @Column("VALOR_CALCULADO")
    private BigDecimal valorCalculado;
    @Column("VALOR_RECEBIDO")
    private BigDecimal valorRecebido;
    @Column("VALOR_FINAL")
    private BigDecimal valorFinal;
    @Column("INDICE_ID")
    private Long idIndice;
    @Transient
    private List<ArrecadacaoMovimento> movimentos = new ArrayList<>();

    public Arrecadacao(ArrecadacaoMovimento movimento) {

        LocalDate dataCompetencia = LocalDate.of(
                Integer.parseInt(movimento.getAnoReferencia()),
                Integer.parseInt(movimento.getMesReferencia()),
                1);

        LocalDate dtVencimento = ChronoUnit.DAYS.addTo(ChronoUnit.MONTHS.addTo(dataCompetencia, 1), -1);

        this.idContribuicao = movimento.getIdContribuicao();
        this.anoCompetencia = movimento.getAnoReferencia();
        this.mesCompetencia = movimento.getMesReferencia();
        this.dataVencimento = dtVencimento;
        this.dataPagamento = dtVencimento;
        this.valorCalculado = movimento.getValorCalculado();
        this.valorRecebido = movimento.getValorRecebido();
        this.valorFinal = movimento.getValorFinal();
        this.idPerfilInvestimento = movimento.getIdPerfilInvestimento();
        this.idIndice = movimento.getIdIndiceArrecadacao();
        this.movimentos.add(movimento);
    }

    public Arrecadacao zerarValores() {
        this.valorCalculado = BigDecimal.ZERO;
        this.valorRecebido = BigDecimal.ZERO;
        this.valorFinal = BigDecimal.ZERO;
        return this;
    }
}
