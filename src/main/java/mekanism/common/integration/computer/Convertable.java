package mekanism.common.integration.computer;

import java.util.function.BiFunction;

/**
 * Created by Thiakil on 21/07/2023.
 */
public class Convertable<RAW> {
    private final RAW value;
    private final BiFunction<FancyComputerHelper, RAW, Object> converter;

    public Convertable(RAW value, BiFunction<FancyComputerHelper, RAW, Object> converter) {
        this.value = value;
        this.converter = converter;
    }

    public Object convert(FancyComputerHelper helper) {
        return converter.apply(helper, value);
    }

    public static <RAW> Convertable<RAW> of(RAW value, BiFunction<FancyComputerHelper, RAW, Object> converter) {
        return new Convertable<>(value, converter);
    }
}
