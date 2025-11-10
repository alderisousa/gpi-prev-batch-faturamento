package br.com.gpi.faturamento.strategy.calculo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametroSomarContribuicaoVO {

    private List<Long> idsContribuicao;
    private List<Long> idsTipoContribuicao;
    private List<FiltroParametroSomarContribuicaoVO> filtros;
    private boolean isAplicaPercentual;
    private List<FaixaContribuicaoVO> faixasContribuicoes;
    private Integer qtDecimais;
}
