package mekanism.common.attachments.containers.fluid;

import com.mojang.serialization.Codec;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.LargeResourceStack;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedFluidTank extends ComponentBackedResourceContainer<FluidResource> implements IFluidTank {

    private final IntSupplier capacity;
    private final IntSupplier rate;

    public ComponentBackedFluidTank(ItemStack attachedTo, int tankIndex, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, IntSupplier rate, IntSupplier capacity) {
        super(attachedTo, tankIndex, capacity.getAsInt(), canExtract, canInsert, validator);
        //TODO - 26.1: Support long capacity
        this.capacity = capacity;
        this.rate = rate;
    }

    @Override
    protected FluidResource getEmptyResource() {
        return FluidResource.EMPTY;
    }

    @Override
    protected Codec<LargeResourceStack<FluidResource>> getResourceStackCodec() {
        return SerializerHelper.FLUID_RESOURCE_STACK_CODEC;
    }

    @Override
    protected ContainerType<?, AttachedResources<FluidResource>, ?> containerType() {
        return ContainerType.FLUID;
    }

    @Override
    public long getLimitAsLong(FluidResource resource) {
        return capacity.getAsInt();
    }

    @Override
    protected int getInsertionRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? super.getInsertionRate(automationType) : rate.getAsInt();
    }

    @Override
    protected int getExtractionRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? super.getExtractionRate(automationType) : rate.getAsInt();
    }
}