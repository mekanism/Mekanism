package mekanism.common.registration.impl;

import mekanism.common.registration.INamedEntry;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerTypeDeferredRegister extends WrappedDeferredRegister<ContainerType<?>> {

    public ContainerTypeDeferredRegister(String modid) {
        super(modid, ForgeRegistries.CONTAINERS);
    }

    public <C extends Container> ContainerTypeRegistryObject<C> register(INamedEntry nameProvider, IContainerFactory<C> factory) {
        return register(nameProvider.getInternalRegistryName(), factory);
    }

    public <C extends Container> ContainerTypeRegistryObject<C> register(String name, IContainerFactory<C> factory) {
        return register(name, () -> IForgeContainerType.create(factory), ContainerTypeRegistryObject::new);
    }
}