package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import mekanism.api.functions.ShortSupplier;
import mekanism.common.network.container.PacketUpdateContainerShort;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling shorts
 */
public abstract class SyncableShort implements ISyncableData<PacketUpdateContainerShort> {

    private short lastKnownValue;

    public abstract short get();

    public abstract void set(short value);

    @Override
    public boolean isDirty() {
        short oldValue = this.get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return dirty;
    }

    @Override
    public PacketUpdateContainerShort getUpdatePacket(short windowId, short property) {
        return new PacketUpdateContainerShort(windowId, property, get());
    }

    public static SyncableShort create(short[] shortArray, int idx) {
        return new SyncableShort() {
            @Override
            public short get() {
                return shortArray[idx];
            }

            @Override
            public void set(short value) {
                shortArray[idx] = value;
            }
        };
    }

    public static SyncableShort create(ShortSupplier getter, ShortConsumer setter) {
        return new SyncableShort() {

            @Override
            public short get() {
                return getter.getAsShort();
            }

            @Override
            public void set(short value) {
                setter.accept(value);
            }
        };
    }

    public static SyncableShort single() {
        return new SyncableShort() {
            private short value;

            @Override
            public short get() {
                return this.value;
            }

            @Override
            public void set(short value) {
                this.value = value;
            }
        };
    }
}