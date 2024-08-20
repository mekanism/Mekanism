package mekanism.common.integration.computer;

import mekanism.api.chemical.ChemicalStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.BiFunction;

/**
 * Wrapper type for a method which may return a different static type at runtime. Ensures that the result can still be converted.
 */
public final class Convertable<RAW> {

    private final RAW value;
    private final BiFunction<BaseComputerHelper, RAW, Object> converter;

    private Convertable(RAW value, BiFunction<BaseComputerHelper, RAW, Object> converter) {
        this.value = value;
        this.converter = converter;
    }

    public Object convert(BaseComputerHelper helper) {
        return converter.apply(helper, value);
    }

    public static <RAW> Convertable<RAW> of(RAW value, BiFunction<BaseComputerHelper, RAW, Object> converter) {
        return new Convertable<>(value, converter);
    }

    public static Convertable<FluidStack> of(FluidStack value) {
        return of(value, BaseComputerHelper::convert);
    }

    public static Convertable<ChemicalStack> of(ChemicalStack value) {
        return of(value, BaseComputerHelper::convert);
    }
}
