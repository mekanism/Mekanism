package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;

public interface IGasProvider extends IBaseProvider {

    @Nonnull
    Gas getGas();

    @Nonnull
    default GasStack getGasStack(int size) {
        return new GasStack(getGas(), size);
    }

    @Override
    default ResourceLocation getRegistryName() {
        return getGas().getRegistryName();
    }

    @Override
    default String getTranslationKey() {
        return getGas().getTranslationKey();
    }

    @Nonnull
    default Fluid getFluid() {
        Gas gas = getGas();
        return gas.hasFluid() ? gas.getFluid() : Fluids.EMPTY;
    }
}