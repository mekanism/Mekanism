package mekanism.common.registries;

import java.util.UUID;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.DataSerializerDeferredRegister;
import mekanism.common.registration.impl.DataSerializerRegistryObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class MekanismDataSerializers {

    public static final DataSerializerDeferredRegister DATA_SERIALIZERS = new DataSerializerDeferredRegister(Mekanism.MODID);

    @SuppressWarnings("rawtypes")
    private static final DataSerializerRegistryObject REGISTRY_ENTRY = registerRegistryEntry();
    public static final DataSerializerRegistryObject<SecurityMode> SECURITY = DATA_SERIALIZERS.registerEnum("security", SecurityMode.class);
    public static final DataSerializerRegistryObject<UUID> UUID = DATA_SERIALIZERS.registerSimple("uuid", FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);

    @SuppressWarnings("unchecked")
    public static <ENTRY extends IForgeRegistryEntry<ENTRY>> EntityDataSerializer<ENTRY> getRegistryEntrySerializer() {
        return (EntityDataSerializer<ENTRY>) REGISTRY_ENTRY.getSerializer();
    }

    @SuppressWarnings("rawtypes")
    private static <ENTRY extends IForgeRegistryEntry<ENTRY>> DataSerializerRegistryObject registerRegistryEntry() {
        return DATA_SERIALIZERS.<ENTRY>registerSimple("registry_entry", IForgeFriendlyByteBuf::writeRegistryId, IForgeFriendlyByteBuf::readRegistryId);
    }
}