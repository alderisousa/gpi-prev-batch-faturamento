package br.com.gpi.faturamento.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DadosParticipante {

    private Long idContribuicao;
    private Long idContribuicaoPai;
    private String nmContribuicao;
    private Long idPessoa;
    private Long idPessoaPlano;
    private Long idPessoaContribuicao;
    private Long idIndice;
    private Long idVinculo;
    private Long idSituacaoVinculo;
    private LocalDate dataOpcao;
    private LocalDate dataNascimento;
    private BigDecimal salarioContribuicao;
    private LocalDate dataAdmissao;
    private BigDecimal valorUR;
    private Long idTipoContribuicao;
    private Long idAtribuicaoPagamento;
    private Long idRotinaElegibilidade;
    private Long idPerfilInvestimento;
    private String nmRotinaElegibilidade;
    private String jsonParametroElegibilidade;
    private Long idRotinaCalculo;
    private String nmRotinaCalculo;
    private String jsonParametroCalculo;
    private Long idRotinaRetorno;
    private String nmRotinaRetorno;
    private String jsonParametroRetorno;
    private String distribuicaoFlag;
    private String salarioParticipacaoFlag;
    private BigDecimal qtdIndiceContribuicao;
    private BigDecimal pctContribuicao;
    private String mes13;
    private String incide13Flag;
    private String dobra13Flag;
    private String contrib13Flag;
    private BigDecimal pct13;

}