package mekanism.common.registries;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.DataSerializerDeferredRegister;
import mekanism.common.registration.impl.DataSerializerRegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.extensions.IFriendlyByteBufExtension;
import net.neoforged.neoforge.registries.IForgeRegistry;

public class MekanismDataSerializers {

    public static final DataSerializerDeferredRegister DATA_SERIALIZERS = new DataSerializerDeferredRegister(Mekanism.MODID);

    public static final DataSerializerRegistryObject<ResourceKey<RobitSkin>> ROBIT_SKIN = registerResourceKey("robit_skin", MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
    public static final DataSerializerRegistryObject<SecurityMode> SECURITY = DATA_SERIALIZERS.registerEnum("security", SecurityMode.class);
    public static final DataSerializerRegistryObject<UUID> UUID = DATA_SERIALIZERS.registerSimple("uuid", FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);

    private static <TYPE> DataSerializerRegistryObject<TYPE> registerRegistryEntry(String name, Supplier<IForgeRegistry<TYPE>> registrySupplier) {
        return DATA_SERIALIZERS.registerSimple(name, (buf, entry) -> buf.writeRegistryId(registrySupplier.get(), entry),
              IFriendlyByteBufExtension::readRegistryId);
    }

    private static <TYPE> DataSerializerRegistryObject<ResourceKey<TYPE>> registerResourceKey(String name, ResourceKey<? extends Registry<TYPE>> registryName) {
        return DATA_SERIALIZERS.registerSimple(name, FriendlyByteBuf::writeResourceKey, buf -> buf.readResourceKey(registryName));
    }
}