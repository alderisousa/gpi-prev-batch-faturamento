package br.com.gpi.faturamento.strategy.elegibilidade;

import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.strategy.elegibilidade.vo.ListaParametroElegibilidadeLoteVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("ELEGIBILIDADES")
public class ElegibilidadePorLoteStrategy implements RotinaElegibilidadeStrategy {

    @Autowired
    private Map<String, RotinaElegibilidadeStrategy> strategies;

    @Override
    public boolean executar(DadosParticipante dados) {
        return executar(dados, dados.getJsonParametroElegibilidade());
    }

    @Override
    public boolean executar(DadosParticipante dados, String parametros) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            var parametro = mapper.readValue(parametros, ListaParametroElegibilidadeLoteVO.class);

            if (parametro.getRotinas() != null) {
                for (var parametroRotina : parametro.getRotinas()) {
                    if (!strategies.get(parametroRotina.getRotina())
                            .executar(dados, parametroRotina.getParametros())) {
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
