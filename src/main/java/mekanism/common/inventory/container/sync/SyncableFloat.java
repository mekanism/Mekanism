package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.network.to_client.container.property.FloatPropertyData;
import net.minecraft.core.RegistryAccess;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling floats
 */
public abstract class SyncableFloat implements ISyncableData {

    private float lastKnownValue;

    public abstract float get();

    public abstract void set(float value);

    @Override
    public DirtyType isDirty() {
        float oldValue = get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return DirtyType.get(dirty);
    }

    @Override
    public FloatPropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        return new FloatPropertyData(property, get());
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
}