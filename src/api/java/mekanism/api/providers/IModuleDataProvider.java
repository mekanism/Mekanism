package mekanism.api.providers;

import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
@MethodsReturnNonnullByDefault
@Deprecated(forRemoval = true, since = "10.7.11")
public interface IModuleDataProvider<MODULE extends ICustomModule<MODULE>> extends IBaseProvider {

    /**
     * Gets the module data this provider represents.
     */
    ModuleData<MODULE> getModuleData();

    @Override
    default ResourceLocation getRegistryName() {
        return Objects.requireNonNull(MekanismAPI.MODULE_REGISTRY.getKey(getModuleData()), "Unregistered module data");
    }

    @Override
    default String getTranslationKey() {
        return getModuleData().getTranslationKey();
    }
}