package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface IChemicalProvider<CHEMICAL extends Chemical<CHEMICAL>> extends IBaseProvider {

    @Nonnull
    CHEMICAL getChemical();

    @Nonnull
    ChemicalStack<CHEMICAL> getStack(long size);

    @Override
    default ResourceLocation getRegistryName() {
        return getChemical().getRegistryName();
    }

    @Override
    default ITextComponent getTextComponent() {
        return getChemical().getTextComponent();
    }

    @Override
    default String getTranslationKey() {
        return getChemical().getTranslationKey();
    }
}