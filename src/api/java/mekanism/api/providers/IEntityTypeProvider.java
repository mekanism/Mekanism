package mekanism.api.providers;

import javax.annotation.Nonnull;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public interface IEntityTypeProvider extends IBaseProvider {

    /**
     * Gets the entity type this provider represents.
     */
    @Nonnull
    EntityType<?> getEntityType();

    @Override
    default ResourceLocation getRegistryName() {
        return getEntityType().getRegistryName();
    }

    @Override
    default Component getTextComponent() {
        return getEntityType().getDescription();
    }

    @Override
    default String getTranslationKey() {
        return getEntityType().getDescriptionId();
    }
}