package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class CombinerIRecipe extends CombinerRecipe {

    public CombinerIRecipe(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        super(id, mainInput, extraInput, output);
    }

    @Override
    public RecipeType<CombinerRecipe> getType() {
        return MekanismRecipeType.COMBINING.get();
    }

    @Override
    public RecipeSerializer<CombinerRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMBINING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.COMBINER.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.COMBINER.getItemStack();
    }
}