package br.com.gpi.faturamento.strategy.elegibilidade;

import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.strategy.elegibilidade.vo.ParametroElegibilidadeTempoVO;
import br.com.gpi.faturamento.utils.DateUtils;
import br.com.gpi.faturamento.utils.NumberUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component("ELEGIBILIDADE_POR_TEMPO")
public class ElegibilidadePorTempoStrategy implements RotinaElegibilidadeStrategy {

    @Override
    public boolean executar(DadosParticipante dados) {
        return executar(dados, dados.getJsonParametroElegibilidade());
    }

    @Override
    public boolean executar(DadosParticipante dados, String parametros) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ParametroElegibilidadeTempoVO parametro = mapper.readValue(parametros, ParametroElegibilidadeTempoVO.class);

            int idade = DateUtils.calculaAnoAteHoje(dados.getDataNascimento());
            int tempoEmpresa = DateUtils.calculaAnoAteHoje(dados.getDataAdmissao());

            return NumberUtils.between(idade, parametro.getIdadeMinima(), parametro.getIdadeMaxima())
                    || NumberUtils.between(tempoEmpresa, parametro.getTempoEmpresaMinimo(), parametro.getTempoEmpresaMaximo());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
