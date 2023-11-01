package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.registries.RegistryObject;

public class StructureModifierSerializerRegistryObject<T extends StructureModifier> extends WrappedRegistryObject<Codec<T>> {

    public StructureModifierSerializerRegistryObject(RegistryObject<Codec<T>> registryObject) {
        super(registryObject);
    }
}