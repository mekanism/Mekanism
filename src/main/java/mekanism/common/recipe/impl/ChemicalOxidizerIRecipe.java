package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ChemicalOxidizerIRecipe extends ItemStackToGasRecipe {

    public ChemicalOxidizerIRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public RecipeType<ItemStackToGasRecipe> getType() {
        return MekanismRecipeType.OXIDIZING.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<ItemStackToGasRecipe> getSerializer() {
        return MekanismRecipeSerializers.OXIDIZING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_OXIDIZER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_OXIDIZER.getItemStack();
    }
}