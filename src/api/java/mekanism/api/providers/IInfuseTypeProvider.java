package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.infuse.InfuseType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface IInfuseTypeProvider extends IBaseProvider {

    @Nonnull
    InfuseType getInfuseType();

    @Override
    default ResourceLocation getRegistryName() {
        return getInfuseType().getRegistryName();
    }

    @Override
    default ITextComponent getTextComponent() {
        return getInfuseType().getTextComponent();
    }

    @Override
    default String getTranslationKey() {
        return getInfuseType().getTranslationKey();
    }
}