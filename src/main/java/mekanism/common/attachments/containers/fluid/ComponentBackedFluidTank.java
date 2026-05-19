package mekanism.common.attachments.containers.fluid;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

@NothingNullByDefault
public class ComponentBackedFluidTank extends ComponentBackedResourceContainer<FluidResource> implements IFluidTank {

    public ComponentBackedFluidTank(ItemStack attachedTo, int tankIndex, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, IntSupplier rate, LongSupplier capacity) {
        super(attachedTo, tankIndex, canExtract, canInsert, validator, rate, capacity);
    }

    @Override
    protected ContainerType<?, AttachedResources<FluidResource>, ?> containerType() {
        return ContainerType.FLUID;
    }
}