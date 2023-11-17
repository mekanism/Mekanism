package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.api.robit.RobitSkin;
import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RobitSkinSerializerRegistryObject<ROBIT_SKIN extends RobitSkin> extends WrappedRegistryObject<Codec<? extends RobitSkin>, Codec<ROBIT_SKIN>> {

    public RobitSkinSerializerRegistryObject(DeferredHolder<Codec<? extends RobitSkin>, Codec<ROBIT_SKIN>> registryObject) {
        super(registryObject);
    }
}