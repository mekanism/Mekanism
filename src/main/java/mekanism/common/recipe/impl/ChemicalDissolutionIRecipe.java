package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class ChemicalDissolutionIRecipe extends BasicChemicalDissolutionRecipe {

    public ChemicalDissolutionIRecipe(ItemStackIngredient itemInput, GasStackIngredient gasInput, ChemicalStack<?> output) {
        super(itemInput, gasInput, output);
    }

    @Override
    public RecipeSerializer<ChemicalDissolutionIRecipe> getSerializer() {
        return MekanismRecipeSerializers.DISSOLUTION.get();
    }

}