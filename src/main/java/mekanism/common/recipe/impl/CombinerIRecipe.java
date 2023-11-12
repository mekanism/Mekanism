package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class CombinerIRecipe extends BasicCombinerRecipe {

    public CombinerIRecipe(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        super(mainInput, extraInput, output);
    }

    @Override
    public RecipeSerializer<BasicCombinerRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMBINING.get();
    }

}