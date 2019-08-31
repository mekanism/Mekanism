package mekanism.api.providers;

import mekanism.api.infuse.InfuseType;
import net.minecraft.util.ResourceLocation;

public interface IInfuseTypeProvider extends IBaseProvider {

    InfuseType getInfuseType();

    @Override
    default ResourceLocation getRegistryName() {
        return getInfuseType().getRegistryName();
    }

    @Override
    default String getTranslationKey() {
        return getInfuseType().getTranslationKey();
    }
}