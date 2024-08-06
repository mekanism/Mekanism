package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientConverter;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientCreator;
import java.util.function.Function;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.resources.ResourceLocation;

class JeiChemicalIngredientConverter implements JeiIngredientConverter<ChemicalStack, ICrTChemicalStack> {

    private final Function<ChemicalStack, ICrTChemicalStack> converter;

    JeiChemicalIngredientConverter(Function<ChemicalStack, ICrTChemicalStack> converter) {
        this.converter = converter;
    }

    @Override
    public JeiIngredientCreator.Creator<ChemicalStack, ICrTChemicalStack> toFullIngredientFromJei(JeiIngredientCreator.FromJei creator, ChemicalStack jeiType) {
        return creator.of(jeiType, ChemicalStack::copy);
    }

    @Override
    public JeiIngredientCreator.Creator<ChemicalStack, ICrTChemicalStack> toFullIngredientFromZen(JeiIngredientCreator.FromZen creator, ICrTChemicalStack zenType) {
        return creator.of(zenType.asImmutable());
    }

    @Override
    public JeiIngredientCreator.Creator<ChemicalStack, ICrTChemicalStack> toFullIngredientFromBoth(JeiIngredientCreator.FromBoth creator, ChemicalStack jeiType, ICrTChemicalStack zenType) {
        return creator.of(jeiType, ChemicalStack::copy, zenType.asImmutable());
    }

    @Override
    public ChemicalStack toJeiFromZen(ICrTChemicalStack zenType) {
        return zenType.getInternal();
    }

    @Override
    public ICrTChemicalStack toZenFromJei(ChemicalStack jeiType) {
        return converter.apply(jeiType);
    }

    @Override
    public String toCommandStringFromZen(ICrTChemicalStack zenType) {
        return zenType.getCommandString();
    }

    @Override
    public ResourceLocation toRegistryNameFromJei(ChemicalStack jeiType) {
        return jeiType.getTypeRegistryName();
    }
}