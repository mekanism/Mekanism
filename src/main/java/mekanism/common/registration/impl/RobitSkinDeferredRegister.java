package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.registration.WrappedDatapackDeferredRegister;

public class RobitSkinDeferredRegister extends WrappedDatapackDeferredRegister<RobitSkin> {

    public RobitSkinDeferredRegister(String modid) {
        super(modid, MekanismAPI.ROBIT_SKIN_SERIALIZER_REGISTRY_NAME, MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
    }

    public <ROBIT_SKIN extends RobitSkin> RobitSkinSerializerRegistryObject<ROBIT_SKIN> registerSerializer(String name, Supplier<Codec<ROBIT_SKIN>> sup) {
        return register(name, sup, RobitSkinSerializerRegistryObject::new);
    }
}