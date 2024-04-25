package mekanism.common.inventory.container.sync;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import net.minecraft.core.RegistryAccess;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling longs
 */
public abstract class SyncableLong implements ISyncableData {

    private long lastKnownValue;

    public abstract long get();

    public abstract void set(long value);

    @Override
    public DirtyType isDirty() {
        long oldValue = get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return DirtyType.get(dirty);
    }

    @Override
    public LongPropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        return new LongPropertyData(property, get());
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
}