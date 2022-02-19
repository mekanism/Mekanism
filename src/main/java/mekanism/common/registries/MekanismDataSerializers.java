package mekanism.common.registries;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.DataSerializerDeferredRegister;
import mekanism.common.registration.impl.DataSerializerRegistryObject;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraftforge.common.extensions.IForgePacketBuffer;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class MekanismDataSerializers {

    public static final DataSerializerDeferredRegister DATA_SERIALIZERS = new DataSerializerDeferredRegister(Mekanism.MODID);

    @SuppressWarnings("rawtypes")
    private static final DataSerializerRegistryObject REGISTRY_ENTRY = registerRegistryEntry();
    public static final DataSerializerRegistryObject<UUID> UUID = DATA_SERIALIZERS.registerSimple("uuid", PacketBuffer::writeUUID, PacketBuffer::readUUID);

    @SuppressWarnings("unchecked")
    public static <ENTRY extends IForgeRegistryEntry<ENTRY>> IDataSerializer<ENTRY> getRegistryEntrySerializer() {
        return (IDataSerializer<ENTRY>) REGISTRY_ENTRY.getSerializer();
    }

    @SuppressWarnings("rawtypes")
    private static <ENTRY extends IForgeRegistryEntry<ENTRY>> DataSerializerRegistryObject registerRegistryEntry() {
        return DATA_SERIALIZERS.<ENTRY>registerSimple("registry_entry", IForgePacketBuffer::writeRegistryId, IForgePacketBuffer::readRegistryId);
    }
}