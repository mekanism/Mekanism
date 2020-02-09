package mekanism.common.inventory.container.sync;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import mekanism.common.network.container.PacketUpdateContainerLong;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling longs
 */
public abstract class SyncableLong implements ISyncableData<PacketUpdateContainerLong> {

    private long lastKnownValue;

    public abstract long get();

    public abstract void set(long value);

    @Override
    public boolean isDirty() {
        long oldValue = this.get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return dirty;
    }

    @Override
    public PacketUpdateContainerLong getUpdatePacket(short windowId, short property) {
        return new PacketUpdateContainerLong(windowId, property, get());
    }

    public static SyncableLong create(long[] longArray, int idx) {
        return new SyncableLong() {
            @Override
            public long get() {
                return longArray[idx];
            }

            @Override
            public void set(long value) {
                longArray[idx] = value;
            }
        };
    }

    public static SyncableLong create(LongSupplier getter, LongConsumer setter) {
        return new SyncableLong() {

            @Override
            public long get() {
                return getter.getAsLong();
            }

            @Override
            public void set(long value) {
                setter.accept(value);
            }
        };
    }

    public static SyncableLong single() {
        return new SyncableLong() {
            private long value;

            @Override
            public long get() {
                return this.value;
            }

            @Override
            public void set(long value) {
                this.value = value;
            }
        };
    }
}