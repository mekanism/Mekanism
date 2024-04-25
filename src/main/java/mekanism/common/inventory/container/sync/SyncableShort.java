package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import mekanism.api.functions.ShortSupplier;
import mekanism.common.network.to_client.container.property.ShortPropertyData;
import net.minecraft.core.RegistryAccess;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling shorts
 */
public abstract class SyncableShort implements ISyncableData {

    private short lastKnownValue;

    public abstract short get();

    public abstract void set(short value);

    @Override
    public DirtyType isDirty() {
        short oldValue = get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return DirtyType.get(dirty);
    }

    @Override
    public ShortPropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        return new ShortPropertyData(property, get());
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
}