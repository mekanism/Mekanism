package mekanism.common.inventory.container.sync.map;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.container.property.ByteArrayPropertyData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;

/// Version of [net.minecraft.world.inventory.DataSlot] for handling maps
public abstract class SyncableMap<KEY, VALUE, MAP extends Map<KEY, VALUE>> implements ISyncableData {

    private final Supplier<MAP> getter;
    private final Consumer<MAP> setter;
    private int lastKnownHashCode;

    protected SyncableMap(Supplier<MAP> getter, Consumer<MAP> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public MAP get() {
        return getter.get();
    }

    public void set(RegistryAccess registryAccess, byte[] rawData) {
        setter.accept(PacketUtils.read(registryAccess, rawData, streamCodec()::decode));
    }

    protected abstract StreamCodec<RegistryFriendlyByteBuf, MAP> streamCodec();

    @Override
    public ByteArrayPropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        //Note: We write it to a byte array so that we make sure to effectively copy it (force a serialization and deserialization)
        // whenever we send this as a packet rather than potentially allowing the list to leak from one side to the other in single player
        byte[] rawData = FriendlyByteBufUtil.writeCustomData(buffer -> streamCodec().encode(buffer, get()), registryAccess);
        return new ByteArrayPropertyData(property, rawData);
    }

    @Override
    public DirtyType isDirty() {
        int valuesHashCode = get().hashCode();
        if (lastKnownHashCode == valuesHashCode) {
            return DirtyType.CLEAN;
        }
        //TODO: Create a way to declare changes so we don't have to sync the entire map, when a single element changes. Both for removal as well as addition
        lastKnownHashCode = valuesHashCode;
        return DirtyType.DIRTY;
    }
}