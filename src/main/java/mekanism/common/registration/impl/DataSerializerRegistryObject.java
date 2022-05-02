package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.RegistryObject;

public class DataSerializerRegistryObject<T> extends WrappedRegistryObject<DataSerializerEntry> {

    public DataSerializerRegistryObject(RegistryObject<DataSerializerEntry> registryObject) {
        super(registryObject);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public EntityDataSerializer<T> getSerializer() {
        return (EntityDataSerializer<T>) get().getSerializer();
    }
}