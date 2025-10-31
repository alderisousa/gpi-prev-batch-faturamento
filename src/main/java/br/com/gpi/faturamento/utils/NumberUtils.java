package br.com.gpi.faturamento.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Optional;

@UtilityClass
public class NumberUtils {

    public static final int DECIMAL_VALOR = 2;

    public static BigDecimal multiplicarValor(BigDecimal m1, BigDecimal m2, Integer qtdDecimais) {
        int scale = qtdDecimais == null ? DECIMAL_VALOR : qtdDecimais;
        return m1.multiply(m2).setScale(scale, RoundingMode.HALF_DOWN);
    }

    public static BigDecimal multiplicarValor(BigDecimal m1, BigDecimal m2) {
        return multiplicarValor(m1, m2, DECIMAL_VALOR);
    }

    public static BigDecimal min(BigDecimal... valores) {
        return Arrays.stream(valores).reduce(BigDecimal::min).orElseThrow();
    }

    public static BigDecimal percentualParaTaxa(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }

        return valor.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal max(BigDecimal... valores) {
        return Arrays.stream(valores).reduce(BigDecimal::max).orElseThrow();
    }

    public static boolean between(int valor, Long minimo, Long maximo) {
        return valor >= minimo && valor <= maximo;
    }

    public static BigDecimal getValorOrZero(BigDecimal valor) {
        return Optional.ofNullable(valor).orElse(BigDecimal.ZERO);
    }
}
