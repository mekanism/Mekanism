package mekanism.common.lib.collection;

import it.unimi.dsi.fastutil.Hash.Strategy;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidHashStrategy implements Strategy<FluidStack> {

    public static final FluidHashStrategy INSTANCE = new FluidHashStrategy();

    private FluidHashStrategy() {
    }

    @Override
    public int hashCode(FluidStack stack) {
        return FluidStack.hashFluidAndComponents(stack);
    }

    @Override
    public boolean equals(FluidStack a, FluidStack b) {
        return FluidStack.isSameFluidSameComponents(a, b);
    }
}