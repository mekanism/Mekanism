package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientConverter;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientCreator;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;

class JeiChemicalIngredientConverter implements JeiIngredientConverter<ChemicalStack, ICrTChemicalStack> {

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
        return new CrTChemicalStack(jeiType);
    }

    @Override
    public String toCommandStringFromZen(ICrTChemicalStack zenType) {
        return zenType.getCommandString();
    }

    @Override
    public ResourceLocation toRegistryNameFromJei(ChemicalStack jeiType) {
        return RegistryUtils.getName(jeiType.getChemicalHolder(), MekanismAPI.CHEMICAL_REGISTRY);
    }
}