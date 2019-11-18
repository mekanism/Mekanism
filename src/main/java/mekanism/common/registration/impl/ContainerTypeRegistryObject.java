package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;

public class ContainerTypeRegistryObject<C extends Container> extends WrappedRegistryObject<ContainerType<C>> {

    public ContainerTypeRegistryObject(RegistryObject<ContainerType<C>> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public ContainerType<C> getContainerType() {
        return get();
    }
}