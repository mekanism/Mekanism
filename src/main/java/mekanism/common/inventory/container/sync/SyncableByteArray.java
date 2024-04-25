package mekanism.common.inventory.container.sync;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.network.to_client.container.property.ByteArrayPropertyData;
import net.minecraft.core.RegistryAccess;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling byte arrays
 */
public class SyncableByteArray implements ISyncableData {

    public static SyncableByteArray create(Supplier<byte[]> getter, Consumer<byte[]> setter) {
        return new SyncableByteArray(getter, setter);
    }

    private final Supplier<byte[]> getter;
    private final Consumer<byte[]> setter;
    private int lastKnownHashCode;

    private SyncableByteArray(Supplier<byte[]> getter, Consumer<byte[]> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public byte[] get() {
        return getter.get();
    }

    public void set(byte[] value) {
        setter.accept(value);
    }

    @Override
    public ByteArrayPropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        //Note: We write it to a byte array so that we make sure to effectively copy it (force a serialization and deserialization)
        // whenever we send this as a packet rather than potentially allowing the array to leak from one side to the other in single player
        byte[] bytes = get();
        return new ByteArrayPropertyData(property, Arrays.copyOf(bytes, bytes.length));
    }

    @Override
    public DirtyType isDirty() {
        int valuesHashCode = Arrays.hashCode(get());
        if (lastKnownHashCode == valuesHashCode) {
            return DirtyType.CLEAN;
        }
        lastKnownHashCode = valuesHashCode;
        return DirtyType.DIRTY;
    }
}