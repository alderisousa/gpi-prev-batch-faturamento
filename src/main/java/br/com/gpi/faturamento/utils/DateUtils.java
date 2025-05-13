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
}
