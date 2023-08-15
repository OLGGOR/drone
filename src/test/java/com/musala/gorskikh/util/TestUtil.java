package com.musala.gorskikh.util;

import com.musala.gorskikh.model.MedicationDto;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class TestUtil {

    @NotNull
    public static MedicationDto createMedication(String code, BigDecimal weight, String name) {
        return new MedicationDto()
                .name(name)
                .weightGr(weight)
                .code(code);
    }

    @NotNull
    public static BigDecimal bd(int value) {
        return BigDecimal.valueOf(value);
    }
}
