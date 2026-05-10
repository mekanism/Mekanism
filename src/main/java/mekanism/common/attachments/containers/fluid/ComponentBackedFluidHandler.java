package mekanism.common.attachments.containers.fluid;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.attachments.containers.ComponentBackedResourceHandler;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

@NothingNullByDefault
public class ComponentBackedFluidHandler extends ComponentBackedResourceHandler<FluidResource, IFluidTank> implements IMekanismFluidHandler {

    public ComponentBackedFluidHandler(ItemStack attachedTo, int totalTanks) {
        super(attachedTo, totalTanks);
    }

    @Override
    protected ContainerType<IFluidTank, AttachedResources<FluidResource>, ?> containerType() {
        return ContainerType.FLUID;
    }
}