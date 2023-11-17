package mekanism.api.providers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

@MethodsReturnNonnullByDefault
public interface IEntityTypeProvider extends IBaseProvider {

    /**
     * Gets the entity type this provider represents.
     */
    EntityType<?> getEntityType();

    @Override
    default ResourceLocation getRegistryName() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getEntityType());
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