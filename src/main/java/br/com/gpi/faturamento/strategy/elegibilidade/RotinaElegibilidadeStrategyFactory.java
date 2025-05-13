package br.com.gpi.faturamento.strategy.elegibilidade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RotinaElegibilidadeStrategyFactory {

    private final Map<String, RotinaElegibilidadeStrategy> strategies;

    public RotinaElegibilidadeStrategy getStrategy(String rotina) {
        RotinaElegibilidadeStrategy strategy = strategies.get(rotina);
        if (strategy == null) {
            throw new IllegalArgumentException("Rotina não existe: " + rotina);
        }
        return strategy;
    }
}
