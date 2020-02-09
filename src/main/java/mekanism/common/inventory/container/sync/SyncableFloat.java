package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.network.container.PacketUpdateContainerFloat;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling floats
 */
public abstract class SyncableFloat implements ISyncableData<PacketUpdateContainerFloat> {

    private float lastKnownValue;

    public abstract float get();

    public abstract void set(float value);

    @Override
    public boolean isDirty() {
        float oldValue = this.get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return dirty;
    }

    @Override
    public PacketUpdateContainerFloat getUpdatePacket(short windowId, short property) {
        return new PacketUpdateContainerFloat(windowId, property, get());
    }

    public static SyncableFloat create(float[] floatArray, int idx) {
        return new SyncableFloat() {
            @Override
            public float get() {
                return floatArray[idx];
            }

            @Override
            public void set(float value) {
                floatArray[idx] = value;
            }
        };
    }

    public static SyncableFloat create(FloatSupplier getter, FloatConsumer setter) {
        return new SyncableFloat() {

            @Override
            public float get() {
                return getter.getAsFloat();
            }

            @Override
            public void set(float value) {
                setter.accept(value);
            }
        };
    }

    public static SyncableFloat single() {
        return new SyncableFloat() {
            private float value;

            @Override
            public float get() {
                return this.value;
            }

            @Override
            public void set(float value) {
                this.value = value;
            }
        };
    }
}