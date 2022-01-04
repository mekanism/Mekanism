package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DataSerializerEntry;

public class DataSerializerRegistryObject<T> extends WrappedRegistryObject<DataSerializerEntry> {

    public DataSerializerRegistryObject(RegistryObject<DataSerializerEntry> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public EntityDataSerializer<T> getSerializer() {
        return (EntityDataSerializer<T>) get().getSerializer();
    }
}