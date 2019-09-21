package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.infuse.InfuseType;
import net.minecraft.util.ResourceLocation;

public interface IInfuseTypeProvider extends IBaseProvider {

    @Nonnull
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