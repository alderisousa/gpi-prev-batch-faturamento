package br.com.gpi.faturamento.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Data
@Table("PLANO_PROCESSAMENTO")
public class PlanoProcessamento {

    @Id
    @Column("ID")
    private Long id;
    @Column("PLANO_ID")
    private Long idPlano;
    @Column("PESSOA_JURIDICA_ID")
    private Long idPessoaJuridica;
    @Column("ID_STATUS_PLANO_PROCESSAMENTO")
    private Long idStatus;
    @Column("ANO_REFERENCIA")
    private String anoReferencia;
    @Column("MES_REFERENCIA")
    private String mesReferencia;

    @Transient
    private List<ArrecadacaoMovimento> movimentos = new ArrayList<>();
}
