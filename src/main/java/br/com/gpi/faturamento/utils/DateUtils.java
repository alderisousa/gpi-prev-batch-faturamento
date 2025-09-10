package br.com.gpi.faturamento.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.Period;

@UtilityClass
public class DateUtils {

    public static int calculaAnoAteHoje(LocalDate data) {
        if (data == null) {
            return -1;
        }

        Period periodo = Period.between(data, LocalDate.now());
        return periodo.getYears();
    }

    public static LocalDate getCompetencia(String ano, String mes) {
        return LocalDate.of(Integer.parseInt(ano), Integer.parseInt(mes), 1)
                .plusMonths(1)
                .minusDays(1);
    }
}
