package mekanism.common.inventory.container.sync;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import mekanism.common.network.to_client.container.property.DoublePropertyData;
import net.minecraft.core.RegistryAccess;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling doubles
 */
public abstract class SyncableDouble implements ISyncableData {

    private double lastKnownValue;

    public abstract double get();

    public abstract void set(double value);

    @Override
    public DirtyType isDirty() {
        double oldValue = get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return DirtyType.get(dirty);
    }

    @Override
    public DoublePropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        return new DoublePropertyData(property, get());
    }

    public static SyncableDouble create(double[] doubleArray, int idx) {
        return new SyncableDouble() {
            @Override
            public double get() {
                return doubleArray[idx];
            }

            @Override
            public void set(double value) {
                doubleArray[idx] = value;
            }
        };
    }

    public static SyncableDouble create(DoubleSupplier getter, DoubleConsumer setter) {
        return new SyncableDouble() {

            @Override
            public double get() {
                return getter.getAsDouble();
            }

            @Override
            public void set(double value) {
                setter.accept(value);
            }
        };
    }
}