package br.com.gpi.faturamento.strategy.elegibilidade;

import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.strategy.elegibilidade.vo.ListaParametroElegibilidadeStatusVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("ELEGIBILIDADE_POR_STATUS")
public class ElegibilidadePorStatusStrategy implements RotinaElegibilidadeStrategy {

    @Override
    public boolean executar(DadosParticipante dados) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ListaParametroElegibilidadeStatusVO parametro = mapper.readValue(dados.getJsonParametroElegibilidade(), ListaParametroElegibilidadeStatusVO.class);

            boolean isValido = true;
            if (parametro.getStatus() != null) {
                for (var status : parametro.getStatus()) {
                    var method = Arrays.stream(dados.getClass().getMethods()).filter(m -> m.getName().equals(status.getMetodo())).findFirst();
                    if (method.isPresent()) {
                        Object result = method.get().invoke(dados);
                        isValido = result instanceof Long && status.getIds().contains(result);
                    }
                }
            }

            return isValido;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
