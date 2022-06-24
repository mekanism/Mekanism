package mekanism.api.providers;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public interface IModuleDataProvider<MODULE extends ICustomModule<MODULE>> extends IBaseProvider {

    /**
     * Gets the module data this provider represents.
     */
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