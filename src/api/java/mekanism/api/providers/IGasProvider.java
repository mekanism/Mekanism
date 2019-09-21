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
    default GasStack getGasStack() {
        //TODO: Should this default to 1000? or maybe this method should not exist at all
        return getGasStack(1);
    }

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

    default Fluid getFluid() {
        Gas gas = getGas();
        return gas.hasFluid() ? gas.getFluid() : Fluids.EMPTY;
    }
}