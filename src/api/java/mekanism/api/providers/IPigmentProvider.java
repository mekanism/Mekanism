package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface IPigmentProvider extends IBaseProvider {

    @Nonnull
    Pigment getPigment();

    @Nonnull
    default PigmentStack getPigmentStack(long size) {
        return new PigmentStack(getPigment(), size);
    }

    @Override
    default ResourceLocation getRegistryName() {
        return getPigment().getRegistryName();
    }

    @Override
    default ITextComponent getTextComponent() {
        return getPigment().getTextComponent();
    }

    @Override
    default String getTranslationKey() {
        return getPigment().getTranslationKey();
    }
}