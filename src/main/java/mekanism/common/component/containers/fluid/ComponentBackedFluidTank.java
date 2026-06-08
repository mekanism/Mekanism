package mekanism.common.component.containers.fluid;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.component.containers.resource.ComponentBackedResourceContainer;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.ResourceContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

@NothingNullByDefault
public class ComponentBackedFluidTank extends ComponentBackedResourceContainer<FluidResource> implements IFluidTank {

    public ComponentBackedFluidTank(ItemAccess attachedAccess, int tankIndex, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, LongSupplier capacity, IntSupplier rate) {
        super(attachedAccess, tankIndex, canExtract, canInsert, validator, capacity, rate);
    }

    @Override
    protected ResourceContainerType<FluidResource, IFluidTank> containerType() {
        return ContainerType.FLUID;
    }
}