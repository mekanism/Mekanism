package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class SawmillIRecipe extends BasicSawmillRecipe {

    public SawmillIRecipe(ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        super(input, mainOutput, secondaryOutput, secondaryChance);
    }

    @Override
    public RecipeSerializer<BasicSawmillRecipe> getSerializer() {
        return MekanismRecipeSerializers.SAWING.get();
    }

}