package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface ISlurryProvider extends IBaseProvider {

    @Nonnull
    Slurry getSlurry();

    @Nonnull
    default SlurryStack getSlurryStack(long size) {
        return new SlurryStack(getSlurry(), size);
    }

    @Override
    default ResourceLocation getRegistryName() {
        return getSlurry().getRegistryName();
    }

    @Override
    default ITextComponent getTextComponent() {
        return getSlurry().getTextComponent();
    }

    @Override
    default String getTranslationKey() {
        return getSlurry().getTranslationKey();
    }
}