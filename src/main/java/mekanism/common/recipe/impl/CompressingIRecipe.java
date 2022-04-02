package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class CompressingIRecipe extends ItemStackGasToItemStackRecipe {

    public CompressingIRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        super(id, itemInput, gasInput, output);
    }

    @Nonnull
    @Override
    public RecipeType<ItemStackGasToItemStackRecipe> getType() {
        return MekanismRecipeType.COMPRESSING.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<ItemStackGasToItemStackRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMPRESSING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.OSMIUM_COMPRESSOR.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.OSMIUM_COMPRESSOR.getItemStack();
    }
}