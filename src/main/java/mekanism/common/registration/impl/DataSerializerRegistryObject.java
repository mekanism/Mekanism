package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DataSerializerEntry;

public class DataSerializerRegistryObject<T> extends WrappedRegistryObject<DataSerializerEntry> {

    public DataSerializerRegistryObject(RegistryObject<DataSerializerEntry> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public IDataSerializer<T> getSerializer() {
        return (IDataSerializer<T>) get().getSerializer();
    }
}