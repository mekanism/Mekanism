package mekanism.common.registries;

import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.DataSerializerDeferredRegister;
import mekanism.common.registration.impl.DataSerializerRegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;

public class MekanismDataSerializers {

    public static final DataSerializerDeferredRegister DATA_SERIALIZERS = new DataSerializerDeferredRegister(Mekanism.MODID);

    public static final DataSerializerRegistryObject<ResourceKey<RobitSkin>> ROBIT_SKIN = registerResourceKey("robit_skin", MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
    public static final DataSerializerRegistryObject<SecurityMode> SECURITY = DATA_SERIALIZERS.registerEnum("security", SecurityMode.class);
    public static final DataSerializerRegistryObject<UUID> UUID = DATA_SERIALIZERS.registerSimple("uuid", FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);

    private static <TYPE> DataSerializerRegistryObject<ResourceKey<TYPE>> registerResourceKey(String name, ResourceKey<? extends Registry<TYPE>> registryName) {
        return DATA_SERIALIZERS.registerSimple(name, FriendlyByteBuf::writeResourceKey, buf -> buf.readResourceKey(registryName));
    }
}