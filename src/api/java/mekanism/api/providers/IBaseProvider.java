package mekanism.api.providers;

import net.minecraft.util.ResourceLocation;

public interface IBaseProvider {

    ResourceLocation getRegistryName();

    default String getName() {
        return getRegistryName().getPath();
    }

    String getTranslationKey();
}