package mekanism.common.inventory.container.sync;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import mekanism.common.network.container.PacketUpdateContainerInt;

/**
 * Slightly modified version of {@link net.minecraft.util.IntReferenceHolder}
 */
public abstract class SyncableInt implements ISyncableData<PacketUpdateContainerInt> {

    private int lastKnownValue;

    public abstract int get();

    public abstract void set(int value);

    @Override
    public boolean isDirty() {
        int oldValue = this.get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return dirty;
    }

    @Override
    public PacketUpdateContainerInt getUpdatePacket(short windowId, short property) {
        return new PacketUpdateContainerInt(windowId, property, get());
    }

    public static SyncableInt create(int[] intArray, int idx) {
        return new SyncableInt() {
            @Override
            public int get() {
                return intArray[idx];
            }

            @Override
            public void set(int value) {
                intArray[idx] = value;
            }
        };
    }

    public static SyncableInt create(IntSupplier getter, IntConsumer setter) {
        return new SyncableInt() {

            @Override
            public int get() {
                return getter.getAsInt();
            }

            @Override
            public void set(int value) {
                setter.accept(value);
            }
        };
    }

    public static SyncableInt single() {
        return new SyncableInt() {
            private int value;

            @Override
            public int get() {
                return this.value;
            }

            @Override
            public void set(int value) {
                this.value = value;
            }
        };
    }
}