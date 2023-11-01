package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.RegistryObject;

public class DataSerializerRegistryObject<T> extends WrappedRegistryObject<EntityDataSerializer<T>> {

    public DataSerializerRegistryObject(RegistryObject<EntityDataSerializer<T>> registryObject) {
        super(registryObject);
    }
}