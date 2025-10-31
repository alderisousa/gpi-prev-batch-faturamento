package br.com.gpi.faturamento.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@Table("HISTORICO_SALARIAL")
@AllArgsConstructor
@NoArgsConstructor
public class HistoricoSalarial {

    @Id
    @Column("ID")
    private Long id;
    @Column("SALARIO_PARTICIPACAO")
    private BigDecimal vrSalarioParticipacao;
    @Column("DATA_SALARIO")
    private LocalDate dataSalario;
    @Column("VINCULO_ID")
    private Long idVinculo;
    @Column("SALARIO")
    private BigDecimal vrSalario;
}
