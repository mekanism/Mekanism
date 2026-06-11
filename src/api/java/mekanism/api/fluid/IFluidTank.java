package mekanism.api.fluid;

import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

/// Represents a [`resource container`][IResourceContainer] that contains [`fluids`][FluidResource].
public interface IFluidTank extends IResourceContainer<FluidResource> {

    @Override
    @NonExtendable
    default LargeResourceStack.StackHelper<FluidResource> stackHelper() {
        return LargeResourceStack.FLUID_HELPER;
    }
}