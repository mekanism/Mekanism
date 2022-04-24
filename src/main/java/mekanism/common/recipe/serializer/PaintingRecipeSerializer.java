package mekanism.common.recipe.serializer;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;

public class PaintingRecipeSerializer<RECIPE extends PaintingRecipe> extends
      ItemStackChemicalToItemStackRecipeSerializer<Pigment, PigmentStack, PigmentStackIngredient, RECIPE> {

    public PaintingRecipeSerializer(IFactory<Pigment, PigmentStack, PigmentStackIngredient, RECIPE> factory) {
        super(factory);
    }

    @Override
    protected ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.PIGMENT;
    }
}