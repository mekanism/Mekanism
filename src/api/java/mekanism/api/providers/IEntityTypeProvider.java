package mekanism.api.providers;

import javax.annotation.Nonnull;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface IEntityTypeProvider extends IBaseProvider {

    @Nonnull
    EntityType<?> getEntityType();

    @Override
    default ResourceLocation getRegistryName() {
        return getEntityType().getRegistryName();
    }

    @Override
    default ITextComponent getTextComponent() {
        return getEntityType().getName();
    }

    @Override
    default String getTranslationKey() {
        return getEntityType().getTranslationKey();
    }
}