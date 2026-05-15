package mekanism.api.fluid;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IMekanismResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

@NothingNullByDefault
public interface IMekanismFluidHandler extends IMekanismResourceHandler<FluidResource, IFluidTank> {

    @Override
    default FluidResource getEmptyResource() {
        return FluidResource.EMPTY;
    }
}