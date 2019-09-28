package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class ItemStackGasToGasIRecipe extends ItemStackGasToGasRecipe {

    public ItemStackGasToGasIRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, Gas outputGas, int outputGasAmount) {
        super(id, itemInput, gasInput, outputGas, outputGasAmount);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackGasToGasRecipe> getType() {
        return MekanismRecipeType.DISSOLUTION;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackGasToGasRecipe> getSerializer() {
        return MekanismRecipeSerializers.DISSOLUTION;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER.getItemStack();
    }
}