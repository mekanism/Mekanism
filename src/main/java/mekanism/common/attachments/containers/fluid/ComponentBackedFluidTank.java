package mekanism.common.attachments.containers.fluid;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.attachments.containers.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedFluidTank extends ComponentBackedResourceContainer<FluidResource, FluidStack, AttachedFluids> implements IFluidTank {

    private final IntSupplier capacity;
    private final IntSupplier rate;

    public ComponentBackedFluidTank(ItemStack attachedTo, int tankIndex, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, IntSupplier rate, IntSupplier capacity) {
        super(attachedTo, tankIndex, capacity.getAsInt(), canExtract, canInsert, validator);
        this.capacity = capacity;
        this.rate = rate;
    }

    @Override
    protected FluidStack copy(FluidStack toCopy) {
        return toCopy.copy();
    }

    @Override
    protected boolean isEmpty(FluidStack value) {
        return value.isEmpty();
    }

    @Override
    protected ContainerType<?, AttachedFluids, ?> containerType() {
        return ContainerType.FLUID;
    }

    /**
     * @apiNote Try to minimize the number of calls to this method so that we don't have to look up the data component multiple times.
     */
    @Override
    public FluidStack getFluid() {
        return getContents(getAttached());
    }

    @Override
    public void setStack(FluidStack stack) {
        setStackUnchecked(stack);
    }

    @Override
    public void setContentsUnchecked(FluidResource type, int storedAmount) {
        setContents(getAttached(), type, storedAmount);
    }

    @Override
    protected FluidResource asResource(FluidStack stack) {
        return FluidResource.of(stack);
    }

    @Override
    protected int getAmount(FluidStack stack) {
        return stack.amount();
    }

    @Override
    protected void setContents(AttachedFluids attachedFluids, FluidResource type, int storedAmount) {
        setContents(attachedFluids, type.toStack(storedAmount));
    }

    @Override
    public int getLimit(FluidResource resource) {
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

    @Override
    public final int setStackSize(int amount, Action action) {
        AttachedFluids attachedFluids = getAttached();
        return setStackSize(attachedFluids, getContents(attachedFluids), amount, action);
    }

    protected int setStackSize(AttachedFluids attachedFluids, FluidStack stored, int amount, Action action) {
        if (stored.isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setContents(attachedFluids, FluidStack.EMPTY);
            }
            return 0;
        }
        int maxStackSize = getCapacity();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (stored.amount() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        setContents(attachedFluids, stored.copyWithAmount(amount));
        return amount;
    }

    @Override
    public int growStack(int amount, Action action) {
        AttachedFluids attachedFluids = getAttached();
        FluidStack stored = getContents(attachedFluids);
        int current = stored.amount();
        if (current == 0) {
            //"Fail quick" if our stack is empty, so we can't grow it
            return 0;
        } else if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk integer overflow
            //If we are increasing the stack's size, use the insert rate
            amount = Math.min(Math.min(amount, getNeeded(stored)), getInsertionRate(null));
        } else if (amount < 0) {
            //If we are decreasing the stack's size, use the extract rate
            amount = Math.max(amount, -getExtractionRate(null));
        }
        int newSize = setStackSize(attachedFluids, stored,current + amount, action);
        return newSize - current;
    }

    protected int getNeeded(FluidStack stored) {
        //Skip the stack lookup for getNeeded
        return Math.max(0, getCapacity() - stored.amount());
    }

    @Override
    public void serialize(ValueOutput output) {
        FluidStack stored = getFluid();
        if (!stored.isEmpty()) {
            output.store(SerializationConstants.STORED, FluidStack.CODEC, stored);
        }
    }
}