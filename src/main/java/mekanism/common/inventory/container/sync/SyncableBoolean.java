package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.function.BooleanSupplier;
import mekanism.common.network.container.PacketUpdateContainerBoolean;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling booleans
 */
public abstract class SyncableBoolean implements ISyncableData<PacketUpdateContainerBoolean> {

    private boolean lastKnownValue;

    public abstract boolean get();

    public abstract void set(boolean value);

    @Override
    public boolean isDirty() {
        boolean oldValue = this.get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return dirty;
    }

    @Override
    public PacketUpdateContainerBoolean getUpdatePacket(short windowId, short property) {
        return new PacketUpdateContainerBoolean(windowId, property, get());
    }

    public static SyncableBoolean create(boolean[] booleanArray, int idx) {
        return new SyncableBoolean() {
            @Override
            public boolean get() {
                return booleanArray[idx];
            }

            @Override
            public void set(boolean value) {
                booleanArray[idx] = value;
            }
        };
    }

    public static SyncableBoolean create(BooleanSupplier getter, BooleanConsumer setter) {
        return new SyncableBoolean() {

            @Override
            public boolean get() {
                return getter.getAsBoolean();
            }

            @Override
            public void set(boolean value) {
                setter.accept(value);
            }
        };
    }

    public static SyncableBoolean single() {
        return new SyncableBoolean() {
            private boolean value;

            @Override
            public boolean get() {
                return this.value;
            }

            @Override
            public void set(boolean value) {
                this.value = value;
            }
        };
    }
}