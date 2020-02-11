package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.network.container.property.GasStackPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling gas stacks
 */
public abstract class SyncableGasStack implements ISyncableData {

    @Nonnull
    private GasStack lastKnownValue = GasStack.EMPTY;

    @Nonnull
    public abstract GasStack get();

    public abstract void set(@Nonnull GasStack value);

    @Override
    public boolean isDirty() {
        GasStack value = this.get();
        boolean dirty = !value.isStackIdentical(this.lastKnownValue);
        if (dirty) {
            //Make sure to copy it in case our gas stack object is the same object so would be getting modified
            // only do so though if it is dirty, as we don't need to spam object creation
            this.lastKnownValue = value.copy();
        }
        return dirty;
    }

    @Override
    public GasStackPropertyData getPropertyData(short property) {
        return new GasStackPropertyData(property, get());
    }

    public static SyncableGasStack create(GasTank handler) {
        return new SyncableGasStack() {

            @Nonnull
            @Override
            public GasStack get() {
                return handler.getStack();
            }

            @Override
            public void set(@Nonnull GasStack value) {
                handler.setStack(value);
            }
        };
    }

    public static SyncableGasStack create(Supplier<@NonNull GasStack> getter, Consumer<@NonNull GasStack> setter) {
        return new SyncableGasStack() {

            @Nonnull
            @Override
            public GasStack get() {
                return getter.get();
            }

            @Override
            public void set(@Nonnull GasStack value) {
                setter.accept(value);
            }
        };
    }
}