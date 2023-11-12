package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.basic.BasicPaintingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class PaintingIRecipe extends BasicPaintingRecipe {

    public PaintingIRecipe(ItemStackIngredient itemInput, PigmentStackIngredient pigmentInput, ItemStack output) {
        super(itemInput, pigmentInput, output);
    }

    @Override
    public RecipeSerializer<BasicPaintingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PAINTING.get();
    }

}