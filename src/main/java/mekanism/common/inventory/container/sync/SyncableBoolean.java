package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.function.BooleanSupplier;
import mekanism.common.network.to_client.container.property.BooleanPropertyData;
import net.minecraft.core.RegistryAccess;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling booleans
 */
public abstract class SyncableBoolean implements ISyncableData {

    private boolean lastKnownValue;

    public abstract boolean get();

    public abstract void set(boolean value);

    @Override
    public DirtyType isDirty() {
        boolean oldValue = get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return DirtyType.get(dirty);
    }

    @Override
    public BooleanPropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        return new BooleanPropertyData(property, get());
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

    public static SyncableBoolean create(boolean[][] booleanArray, int idx1, int idx2) {
        return new SyncableBoolean() {
            @Override
            public boolean get() {
                return booleanArray[idx1][idx2];
            }

            @Override
            public void set(boolean value) {
                booleanArray[idx1][idx2] = value;
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
}