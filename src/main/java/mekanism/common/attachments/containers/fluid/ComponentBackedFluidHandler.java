package mekanism.common.attachments.containers.fluid;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

@NothingNullByDefault
public class ComponentBackedFluidHandler extends ComponentBackedHandler<FluidStack, IFluidTank, AttachedFluids> implements IMekanismFluidHandler {

    public ComponentBackedFluidHandler(ItemStack attachedTo, int totalTanks) {
        super(attachedTo, totalTanks);
    }

    @Override
    protected ContainerType<IFluidTank, AttachedFluids, ?> containerType() {
        return ContainerType.FLUID;
    }
}