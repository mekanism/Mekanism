package mekanism.common.registries;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.DataSerializerDeferredRegister;
import mekanism.common.registration.impl.DataSerializerRegistryObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;

public class MekanismDataSerializers {

    public static final DataSerializerDeferredRegister DATA_SERIALIZERS = new DataSerializerDeferredRegister(Mekanism.MODID);

    public static final DataSerializerRegistryObject<RobitSkin> ROBIT_SKIN = registerRegistryEntry("robit_skin", MekanismAPI::robitSkinRegistry);
    public static final DataSerializerRegistryObject<SecurityMode> SECURITY = DATA_SERIALIZERS.registerEnum("security", SecurityMode.class);
    public static final DataSerializerRegistryObject<UUID> UUID = DATA_SERIALIZERS.registerSimple("uuid", FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);

    private static <TYPE> DataSerializerRegistryObject<TYPE> registerRegistryEntry(String name, Supplier<IForgeRegistry<TYPE>> registrySupplier) {
        return DATA_SERIALIZERS.registerSimple(name, (buf, entry) -> buf.writeRegistryId(registrySupplier.get(), entry),
              IForgeFriendlyByteBuf::readRegistryId);
    }
}