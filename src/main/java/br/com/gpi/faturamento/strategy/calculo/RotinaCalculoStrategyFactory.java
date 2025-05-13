package br.com.gpi.faturamento.strategy.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RotinaCalculoStrategyFactory {

    private final Map<String, RotinaCalculoStrategy> strategies;

    public RotinaCalculoStrategy getStrategy(String rotina) {
        RotinaCalculoStrategy strategy = strategies.get(rotina);
        if (strategy == null) {
            throw new IllegalArgumentException("Rotina não existe: " + rotina);
        }
        return strategy;
    }
}
