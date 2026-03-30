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

            int idade = DateUtils.calculaAno(dados.getDataNascimento(), dados.getDataCompetencia());
            int tempoEmpresa = DateUtils.calculaAno(dados.getDataAdmissao(), dados.getDataCompetencia());
            int tempoPlano = DateUtils.calculaAno(dados.getDataOpcao(), dados.getDataCompetencia());

            var isIdade = isValido(idade, parametro.getIdadeMinima(), parametro.getIdadeMaxima());
            var isTempoEmpresa = isValido(tempoEmpresa, parametro.getTempoEmpresaMinimo(), parametro.getTempoEmpresaMaximo());
            var isTempoPlano = isValido(tempoPlano, parametro.getTempoPlanoMinimo(), parametro.getTempoPlanoMaximo());

            return isIdade || isTempoEmpresa || isTempoPlano;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    private boolean isValido(int tempo, Long minimo, Long maximo) {
        if (minimo == null || maximo == null)
            return false;

        return NumberUtils.between(tempo, minimo, maximo);
    }
}
