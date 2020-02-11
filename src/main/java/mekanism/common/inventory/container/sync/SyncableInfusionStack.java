package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.infuse.InfusionTank;
import mekanism.common.network.container.property.InfusionStackPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling infusion stacks
 */
public abstract class SyncableInfusionStack implements ISyncableData {

    @Nonnull
    private InfusionStack lastKnownValue = InfusionStack.EMPTY;

    @Nonnull
    public abstract InfusionStack get();

    public abstract void set(@Nonnull InfusionStack value);

    @Override
    public boolean isDirty() {
        InfusionStack value = this.get();
        boolean dirty = !value.isStackIdentical(this.lastKnownValue);
        if (dirty) {
            //Make sure to copy it in case our infusion stack object is the same object so would be getting modified
            // only do so though if it is dirty, as we don't need to spam object creation
            this.lastKnownValue = value.copy();
        }
        return dirty;
    }

    @Override
    public InfusionStackPropertyData getPropertyData(short property) {
        return new InfusionStackPropertyData(property, get());
    }

    public static SyncableInfusionStack create(InfusionTank handler) {
        return new SyncableInfusionStack() {

            @Nonnull
            @Override
            public InfusionStack get() {
                return handler.getStack();
            }

            @Override
            public void set(@Nonnull InfusionStack value) {
                handler.setStack(value);
            }
        };
    }

    public static SyncableInfusionStack create(Supplier<@NonNull InfusionStack> getter, Consumer<@NonNull InfusionStack> setter) {
        return new SyncableInfusionStack() {

            @Nonnull
            @Override
            public InfusionStack get() {
                return getter.get();
            }

            @Override
            public void set(@Nonnull InfusionStack value) {
                setter.accept(value);
            }
        };
    }
}