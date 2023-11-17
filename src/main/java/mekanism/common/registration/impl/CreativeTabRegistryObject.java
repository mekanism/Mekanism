package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CreativeTabRegistryObject extends WrappedRegistryObject<CreativeModeTab, CreativeModeTab> {

    public CreativeTabRegistryObject(DeferredHolder<CreativeModeTab, CreativeModeTab> registryObject) {
        super(registryObject);
    }
}