package mekanism.api.providers;

import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public interface IBaseProvider extends IHasTextComponent, IHasTranslationKey {

    ResourceLocation getRegistryName();

    default String getName() {
        return getRegistryName().getPath();
    }

    @Override
    default ITextComponent getTextComponent() {
        return new TranslationTextComponent(getTranslationKey());
    }
}