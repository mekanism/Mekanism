package mekanism.api.providers;

import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;

public interface IEntityTypeProvider extends IBaseProvider {

    EntityType getEntityType();

    @Override
    default ResourceLocation getRegistryName() {
        return getEntityType().getRegistryName();
    }

    @Override
    default String getTranslationKey() {
        return getEntityType().getTranslationKey();
    }
}