package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import mekanism.api.functions.ByteSupplier;
import mekanism.common.network.container.PacketUpdateContainerByte;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling bytes
 */
public abstract class SyncableByte implements ISyncableData<PacketUpdateContainerByte> {

    private byte lastKnownValue;

    public abstract byte get();

    public abstract void set(byte value);

    @Override
    public boolean isDirty() {
        byte oldValue = this.get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return dirty;
    }

    @Override
    public PacketUpdateContainerByte getUpdatePacket(short windowId, short property) {
        return new PacketUpdateContainerByte(windowId, property, get());
    }

    public static SyncableByte create(byte[] byteArray, int idx) {
        return new SyncableByte() {
            @Override
            public byte get() {
                return byteArray[idx];
            }

            @Override
            public void set(byte value) {
                byteArray[idx] = value;
            }
        };
    }

    public static SyncableByte create(ByteSupplier getter, ByteConsumer setter) {
        return new SyncableByte() {

            @Override
            public byte get() {
                return getter.getAsByte();
            }

            @Override
            public void set(byte value) {
                setter.accept(value);
            }
        };
    }

    public static SyncableByte single() {
        return new SyncableByte() {
            private byte value;

            @Override
            public byte get() {
                return this.value;
            }

            @Override
            public void set(byte value) {
                this.value = value;
            }
        };
    }
}