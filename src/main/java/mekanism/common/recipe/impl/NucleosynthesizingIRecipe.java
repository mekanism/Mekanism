package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.basic.BasicNucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class NucleosynthesizingIRecipe extends BasicNucleosynthesizingRecipe {

    public NucleosynthesizingIRecipe(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output, int duration) {
        super(itemInput, gasInput, output, duration);
    }

    @Override
    public RecipeSerializer<BasicNucleosynthesizingRecipe> getSerializer() {
        return MekanismRecipeSerializers.NUCLEOSYNTHESIZING.get();
    }

}