package mekanism.common.attachments.containers.fluid;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ResourceContainersBuilder;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.config.MekanismConfig;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FluidTanksBuilder extends ResourceContainersBuilder<FluidResource, ComponentBackedFluidTank, FluidTanksBuilder> {

    public static FluidTanksBuilder builder() {
        return new FluidTanksBuilder();
    }

    private FluidTanksBuilder() {
    }

    @Override
    public BaseContainerCreator<AttachedResources<FluidResource>, ComponentBackedFluidTank> build() {
        return new BaseFluidTankCreator(containerCreators);
    }

    @Override
    protected IntSupplier defaultRate() {
        return MekanismConfig.general.fluidItemFillRate;
    }

    @Override
    protected ComponentBackedFluidTank createBasicContainer(ItemStack attachedTo, int tankIndex, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, IntSupplier rate, LongSupplier capacity) {
        return new ComponentBackedFluidTank(attachedTo, tankIndex, canExtract, canInsert, validator, rate, capacity);
    }

    private static class BaseFluidTankCreator extends BaseContainerCreator<AttachedResources<FluidResource>, ComponentBackedFluidTank> {

        public BaseFluidTankCreator(List<IBasicContainerCreator<? extends ComponentBackedFluidTank>> creators) {
            super(creators);
        }

        @Override
        public AttachedResources<FluidResource> initStorage(int containers) {
            return AttachedResources.create(containers, LargeResourceStack.EMPTY_FLUID_STACK);
        }
    }
}