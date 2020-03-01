package mekanism.common.inventory.container.sync;

import javax.annotation.Nonnull;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.network.container.property.FluidStackPropertyData;
import mekanism.common.network.container.property.IntPropertyData;
import mekanism.common.network.container.property.PropertyData;
import net.minecraftforge.fluids.FluidStack;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling fluid stacks
 */
public class SyncableFluidStack implements ISyncableData {

    public static SyncableFluidStack create(@Nonnull IExtendedFluidTank handler) {
        return new SyncableFluidStack(handler);
    }

    @Nonnull
    private final IExtendedFluidTank handler;
    @Nonnull
    private FluidStack lastKnownValue = FluidStack.EMPTY;

    private SyncableFluidStack(@Nonnull IExtendedFluidTank handler) {
        this.handler = handler;
    }

    @Nonnull
    public FluidStack get() {
        return handler.getFluid();
    }

    public void set(@Nonnull FluidStack value) {
        handler.setStack(value);
    }

    public void set(int amount) {
        if (!handler.isEmpty()) {
            //Double check it is not empty
            handler.setStack(new FluidStack(handler.getFluid(), amount));
        }
    }

    @Override
    public DirtyType isDirty() {
        FluidStack value = this.get();
        boolean sameFluid = value.isFluidEqual(this.lastKnownValue);
        if (!sameFluid || value.getAmount() != this.lastKnownValue.getAmount()) {
            //Make sure to copy it in case our fluid stack object is the same object so would be getting modified
            // only do so though if it is dirty, as we don't need to spam object creation
            this.lastKnownValue = value.copy();
            return sameFluid ? DirtyType.SIZE : DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new IntPropertyData(property, get().getAmount());
        }
        return new FluidStackPropertyData(property, get());
    }
}