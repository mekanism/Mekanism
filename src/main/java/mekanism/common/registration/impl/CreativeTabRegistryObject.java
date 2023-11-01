package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.RegistryObject;

public class CreativeTabRegistryObject extends WrappedRegistryObject<CreativeModeTab> {

    public CreativeTabRegistryObject(RegistryObject<CreativeModeTab> registryObject) {
        super(registryObject);
    }
}