package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.api.robit.RobitSkin;
import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.registries.RegistryObject;

public class RobitSkinSerializerRegistryObject<ROBIT_SKIN extends RobitSkin> extends WrappedRegistryObject<Codec<? extends ROBIT_SKIN>> {

    public RobitSkinSerializerRegistryObject(RegistryObject<Codec<? extends ROBIT_SKIN>> registryObject) {
        super(registryObject);
    }
}