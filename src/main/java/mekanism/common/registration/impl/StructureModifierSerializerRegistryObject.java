package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.registries.DeferredHolder;

public class StructureModifierSerializerRegistryObject<T extends StructureModifier> extends WrappedRegistryObject<Codec<? extends StructureModifier>, Codec<T>> {

    public StructureModifierSerializerRegistryObject(DeferredHolder<Codec<? extends StructureModifier>, Codec<T>> registryObject) {
        super(registryObject);
    }
}