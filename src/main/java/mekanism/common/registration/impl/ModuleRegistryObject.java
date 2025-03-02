package mekanism.common.registration.impl;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public class ModuleRegistryObject<MODULE extends ICustomModule<MODULE>> extends MekanismDeferredHolder<ModuleData<?>, ModuleData<MODULE>> implements IHasTranslationKey {

    public ModuleRegistryObject(ResourceKey<ModuleData<?>> key) {
        super(key);
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return get().getTranslationKey();
    }
}