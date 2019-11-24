package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class GasConversionIRecipe extends ItemStackToGasRecipe {

    public GasConversionIRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackToGasRecipe> getType() {
        return MekanismRecipeType.GAS_CONVERSION;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackToGasRecipe> getSerializer() {
        return MekanismRecipeSerializers.GAS_CONVERSION.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "gas_conversion";
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.CREATIVE_GAS_TANK.getItemStack();
    }
}