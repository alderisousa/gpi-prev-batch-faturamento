package br.com.gpi.faturamento.strategy.elegibilidade;

import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.strategy.elegibilidade.vo.ListaParametroElegibilidadeStatusVO;
import br.com.gpi.faturamento.strategy.elegibilidade.vo.ParametroElegibilidadeStatusVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component("ELEGIBILIDADE_POR_STATUS")
public class ElegibilidadePorStatusStrategy implements RotinaElegibilidadeStrategy {

    private ParametroElegibilidadeStatusVO DEFAULT = ParametroElegibilidadeStatusVO.builder()
            .metodo("getIdSituacaoPlano")
            .ids(List.of(1L))
            .build();

    @Override
    public boolean executar(DadosParticipante dados) {
        return executar(dados, dados.getJsonParametroElegibilidade());
    }

    @Override
    public boolean executar(DadosParticipante dados, String parametros) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ListaParametroElegibilidadeStatusVO parametro = mapper.readValue(parametros, ListaParametroElegibilidadeStatusVO.class);
            verificarSobrescritaDefault(parametro);

            boolean isValido = true;
            if (parametro.getStatus() != null) {
                for (var status : parametro.getStatus()) {
                    var method = Arrays.stream(dados.getClass().getMethods()).filter(m -> m.getName().equals(status.getMetodo())).findFirst();
                    if (method.isPresent()) {
                        Object result = method.get().invoke(dados);
                        isValido = isValido && result instanceof Long && status.getIds().contains(result);
                    }
                }
            }

            return isValido;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    private void verificarSobrescritaDefault(ListaParametroElegibilidadeStatusVO parametro) {
        boolean isNaoSobrescrito = parametro.getStatus().stream()
                .noneMatch(s -> s.getMetodo().equals(DEFAULT.getMetodo()));
        if (isNaoSobrescrito)
            parametro.getStatus().add(DEFAULT);
    }
}
