package br.com.gpi.faturamento.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

@UtilityClass
public class NumberUtils {

    public static BigDecimal multiplicarValor(BigDecimal m1, BigDecimal m2) {
        return m1.multiply(m2).setScale(2, RoundingMode.HALF_DOWN);
    }

    public static BigDecimal min(BigDecimal... valores) {
        return Arrays.stream(valores).reduce(BigDecimal::min).orElseThrow();
    }

    public static BigDecimal percentualParaFaixa(BigDecimal valor) {
        return valor.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal max(BigDecimal... valores) {
        return Arrays.stream(valores).reduce(BigDecimal::max).orElseThrow();
    }

    public static boolean between(int valor, Long minimo, Long maximo) {
        return valor >= minimo && valor <= maximo;
    }
}
