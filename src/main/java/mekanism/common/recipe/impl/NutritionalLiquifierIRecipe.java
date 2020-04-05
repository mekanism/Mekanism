package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class NutritionalLiquifierIRecipe extends ItemStackToGasRecipe {

    public NutritionalLiquifierIRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackToGasRecipe> getType() {
        return null;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackToGasRecipe> getSerializer() {
        return null;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.NUTRITIONAL_LIQUIFIER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.NUTRITIONAL_LIQUIFIER.getItemStack();
    }
}