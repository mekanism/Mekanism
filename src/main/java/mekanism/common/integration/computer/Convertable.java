package mekanism.common.integration.computer;

import java.util.function.BiFunction;

/**
 * Created by Thiakil on 21/07/2023.
 */
public class Convertable<RAW> {
    private final RAW value;
    private final BiFunction<BaseComputerHelper, RAW, Object> converter;

    public Convertable(RAW value, BiFunction<BaseComputerHelper, RAW, Object> converter) {
        this.value = value;
        this.converter = converter;
    }

    public Object convert(BaseComputerHelper helper) {
        return converter.apply(helper, value);
    }

    public static <RAW> Convertable<RAW> of(RAW value, BiFunction<BaseComputerHelper, RAW, Object> converter) {
        return new Convertable<>(value, converter);
    }
}
