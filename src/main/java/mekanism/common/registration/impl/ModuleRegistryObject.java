package mekanism.common.registration.impl;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModuleRegistryObject<MODULE extends ICustomModule<MODULE>> extends MekanismDeferredHolder<ModuleData<?>, ModuleData<MODULE>> implements IModuleDataProvider<MODULE>,
      IHasTranslationKey {

    public ModuleRegistryObject(ResourceKey<ModuleData<?>> key) {
        super(key);
    }

    @NotNull
    @Override
    public ModuleData<MODULE> getModuleData() {
        return value();
    }

    @NotNull
    @Override
    public ResourceLocation getRegistryName() {
        return getId();
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return get().getTranslationKey();
    }
}