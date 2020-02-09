package mekanism.common.inventory.container.sync;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import mekanism.common.network.container.PacketUpdateContainerDouble;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling doubles
 */
public abstract class SyncableDouble implements ISyncableData<PacketUpdateContainerDouble> {

    private double lastKnownValue;

    public abstract double get();

    public abstract void set(double value);

    @Override
    public boolean isDirty() {
        double oldValue = this.get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return dirty;
    }

    @Override
    public PacketUpdateContainerDouble getUpdatePacket(short windowId, short property) {
        return new PacketUpdateContainerDouble(windowId, property, get());
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

    public static SyncableDouble single() {
        return new SyncableDouble() {
            private double value;

            @Override
            public double get() {
                return this.value;
            }

            @Override
            public void set(double value) {
                this.value = value;
            }
        };
    }
}