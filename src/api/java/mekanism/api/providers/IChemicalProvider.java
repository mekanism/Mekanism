package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface IChemicalProvider<CHEMICAL extends Chemical<CHEMICAL>> extends IBaseProvider {

    /**
     * Gets the chemical this provider represents.
     */
    @Nonnull
    CHEMICAL getChemical();

    /**
     * Creates a chemical stack of the given size using the chemical this provider represents.
     *
     * @param size Size of the stack.
     */
    @Nonnull
    ChemicalStack<CHEMICAL> getStack(long size);

    @Override
    default ResourceLocation getRegistryName() {
        return getChemical().getRegistryName();
    }

    @Override
    default Component getTextComponent() {
        return getChemical().getTextComponent();
    }

    @Override
    default String getTranslationKey() {
        return getChemical().getTranslationKey();
    }
}