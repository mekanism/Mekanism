package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.container.property.ByteArrayPropertyData;
import net.minecraft.core.RegistryAccess;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling frequencies
 */
public class SyncableFrequency<FREQUENCY extends Frequency> implements ISyncableData {

    public static <FREQUENCY extends Frequency> SyncableFrequency<FREQUENCY> create(FrequencyType<FREQUENCY> type, Supplier<FREQUENCY> getter, Consumer<FREQUENCY> setter) {
        return new SyncableFrequency<>(type, getter, setter);
    }

    private final FrequencyType<FREQUENCY> type;
    private final Supplier<FREQUENCY> getter;
    private final Consumer<FREQUENCY> setter;
    private int lastKnownHashCode;

    private SyncableFrequency(FrequencyType<FREQUENCY> type, Supplier<FREQUENCY> getter, Consumer<FREQUENCY> setter) {
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    @Nullable
    public FREQUENCY get() {
        return getter.get();
    }

    public void set(RegistryAccess registryAccess, byte[] rawData) {
        setter.accept(PacketUtils.read(registryAccess, rawData, buffer -> buffer.readNullable(buf -> type.create(buffer))));
    }

    @Override
    public DirtyType isDirty() {
        FREQUENCY value = get();
        int valueHashCode = value == null ? 0 : value.getSyncHash();
        if (lastKnownHashCode == valueHashCode) {
            return DirtyType.CLEAN;
        }
        lastKnownHashCode = valueHashCode;
        return DirtyType.DIRTY;
    }

    @Override
    public ByteArrayPropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        //Note: We write it to a byte array so that we make sure to effectively copy it (force a serialization and deserialization)
        // whenever we send this as a packet rather than potentially allowing the frequency to leak from one side to the other in single player
        byte[] rawData = FriendlyByteBufUtil.writeCustomData(buffer -> buffer.writeNullable(get(), (buf, val) -> type.streamCodec().encode(buffer, val)), registryAccess);
        return new ByteArrayPropertyData(property, rawData);
    }
}