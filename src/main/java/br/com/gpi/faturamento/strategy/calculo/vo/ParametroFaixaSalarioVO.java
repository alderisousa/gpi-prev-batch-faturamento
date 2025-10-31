package br.com.gpi.faturamento.strategy.calculo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametroFaixaSalarioVO {

    private List<FaixaTempoPlanoVO> faixas;
    private Integer qtDecimais;
}
