package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import net.minecraft.resources.ResourceLocation;

public interface IModuleDataProvider<MODULE extends ICustomModule<MODULE>> extends IBaseProvider {

    /**
     * Gets the module data this provider represents.
     */
    @Nonnull
    ModuleData<MODULE> getModuleData();

    @Override
    default ResourceLocation getRegistryName() {
        return getModuleData().getRegistryName();
    }

    @Override
    default String getTranslationKey() {
        return getModuleData().getTranslationKey();
    }
}