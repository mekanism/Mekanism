package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;

public class ContainerTypeRegistryObject<CONTAINER extends Container> extends WrappedRegistryObject<ContainerType<CONTAINER>> {

    public ContainerTypeRegistryObject(RegistryObject<ContainerType<CONTAINER>> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public ContainerType<CONTAINER> getContainerType() {
        return get();
    }

    //Internal use only overwrite the registry object
    ContainerTypeRegistryObject<CONTAINER> setRegistryObject(RegistryObject<ContainerType<CONTAINER>> registryObject) {
        this.registryObject = registryObject;
        return this;
    }
}