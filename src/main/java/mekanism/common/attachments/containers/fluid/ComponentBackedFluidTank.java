package mekanism.common.attachments.containers.fluid;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.attachments.containers.resource.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.ResourceContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

@NothingNullByDefault
public class ComponentBackedFluidTank extends ComponentBackedResourceContainer<FluidResource> implements IFluidTank {

    public ComponentBackedFluidTank(ItemAccess attachedAccess, int tankIndex, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, IntSupplier rate, LongSupplier capacity) {
        super(attachedAccess, tankIndex, canExtract, canInsert, validator, rate, capacity);
    }

    @Override
    protected ResourceContainerType<FluidResource, IFluidTank> containerType() {
        return ContainerType.FLUID;
    }
}