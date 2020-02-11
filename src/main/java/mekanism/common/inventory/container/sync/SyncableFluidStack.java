package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.common.network.container.property.FluidStackPropertyData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling fluid stacks
 */
public abstract class SyncableFluidStack implements ISyncableData {

    @Nonnull
    private FluidStack lastKnownValue = FluidStack.EMPTY;

    @Nonnull
    public abstract FluidStack get();

    public abstract void set(@Nonnull FluidStack value);

    @Override
    public boolean isDirty() {
        FluidStack value = this.get();
        boolean dirty = !value.isFluidStackIdentical(this.lastKnownValue);
        if (dirty) {
            //Make sure to copy it in case our fluid stack object is the same object so would be getting modified
            // only do so though if it is dirty, as we don't need to spam object creation
            this.lastKnownValue = value.copy();
        }
        return dirty;
    }

    @Override
    public FluidStackPropertyData getPropertyData(short property) {
        return new FluidStackPropertyData(property, get());
    }

    public static SyncableFluidStack create(FluidTank handler) {
        return new SyncableFluidStack() {

            @Nonnull
            @Override
            public FluidStack get() {
                return handler.getFluid();
            }

            @Override
            public void set(@Nonnull FluidStack value) {
                handler.setFluid(value);
            }
        };
    }

    public static SyncableFluidStack create(Supplier<@NonNull FluidStack> getter, Consumer<@NonNull FluidStack> setter) {
        return new SyncableFluidStack() {

            @Nonnull
            @Override
            public FluidStack get() {
                return getter.get();
            }

            @Override
            public void set(@Nonnull FluidStack value) {
                setter.accept(value);
            }
        };
    }
}