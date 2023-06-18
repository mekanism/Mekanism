package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;

public class CreativeTabDeferredRegister extends WrappedDeferredRegister<CreativeModeTab> {

    public CreativeTabDeferredRegister(String modid) {
        super(modid, Registries.CREATIVE_MODE_TAB);
    }

    public CreativeTabRegistryObject register(String name, Supplier<CreativeModeTab.Builder> sup) {
        return register(name, () -> sup.get().build(), CreativeTabRegistryObject::new);
    }
}