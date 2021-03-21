package mekanism.common.recipe.serializer;

import mekanism.api.JsonConstants;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;

public class MetallurgicInfuserRecipeSerializer<RECIPE extends MetallurgicInfuserRecipe> extends
      ItemStackChemicalToItemStackRecipeSerializer<InfuseType, InfusionStack, InfusionStackIngredient, RECIPE> {

    public MetallurgicInfuserRecipeSerializer(IFactory<InfuseType, InfusionStack, InfusionStackIngredient, RECIPE> factory) {
        super(factory);
    }

    @Override
    protected ChemicalIngredientDeserializer<InfuseType, InfusionStack, InfusionStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.INFUSION;
    }

    @Override
    protected String getChemicalInputJsonKey() {
        return JsonConstants.INFUSION_INPUT;
    }
}