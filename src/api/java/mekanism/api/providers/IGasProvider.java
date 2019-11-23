package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

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
    default ITextComponent getTextComponent() {
        return getGas().getTextComponent();
    }

    //TODO: Remove this??
    @Nonnull
    @Deprecated
    default Fluid getFluid() {
        Gas gas = getGas();
        return gas.hasFluid() ? gas.getFluid() : Fluids.EMPTY;
    }
}