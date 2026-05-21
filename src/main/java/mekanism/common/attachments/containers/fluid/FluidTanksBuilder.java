package mekanism.common.attachments.containers.fluid;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ResourceContainersBuilder;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.config.MekanismConfig;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FluidTanksBuilder extends ResourceContainersBuilder<FluidResource, ComponentBackedFluidTank, FluidTanksBuilder> {

    public static FluidTanksBuilder builder() {
        return new FluidTanksBuilder();
    }

    private FluidTanksBuilder() {
    }

    @Override
    public BaseContainerCreator<AttachedResources<FluidResource>, ComponentBackedFluidTank> build() {
        return new BaseContainerBuilder<>(containerCreators, LargeResourceStack.FLUID_HELPER);
    }

    @Override
    protected IntSupplier defaultRate() {
        return MekanismConfig.general.fluidItemFillRate;
    }

    @Override
    protected ComponentBackedFluidTank createBasicContainer(ItemAccess attachedAccess, int tankIndex, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, IntSupplier rate, LongSupplier capacity) {
        return new ComponentBackedFluidTank(attachedAccess, tankIndex, canExtract, canInsert, validator, rate, capacity);
    }
}