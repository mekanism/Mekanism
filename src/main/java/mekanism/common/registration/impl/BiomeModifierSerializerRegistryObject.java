package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BiomeModifierSerializerRegistryObject<T extends BiomeModifier> extends WrappedRegistryObject<Codec<? extends BiomeModifier>, Codec<T>> {

    public BiomeModifierSerializerRegistryObject(DeferredHolder<Codec<? extends BiomeModifier>, Codec<T>> registryObject) {
        super(registryObject);
    }
}