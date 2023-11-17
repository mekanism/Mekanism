package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DataSerializerRegistryObject<T> extends WrappedRegistryObject<EntityDataSerializer<?>, EntityDataSerializer<T>> {

    public DataSerializerRegistryObject(DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<T>> registryObject) {
        super(registryObject);
    }
}