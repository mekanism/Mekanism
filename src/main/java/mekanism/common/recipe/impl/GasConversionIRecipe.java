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

public class GasConversionIRecipe extends ItemStackToGasRecipe {

    public GasConversionIRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public RecipeType<ItemStackToGasRecipe> getType() {
        return MekanismRecipeType.GAS_CONVERSION.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<ItemStackToGasRecipe> getSerializer() {
        return MekanismRecipeSerializers.GAS_CONVERSION.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "gas_conversion";
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemStack();
    }
}