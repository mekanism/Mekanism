package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class ItemStackGasToGasIRecipe extends ItemStackGasToGasRecipe {

    public ItemStackGasToGasIRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, GasStack output) {
        super(id, itemInput, gasInput, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackGasToGasRecipe> getType() {
        return MekanismRecipeType.DISSOLUTION;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackGasToGasRecipe> getSerializer() {
        return MekanismRecipeSerializers.DISSOLUTION.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.getItemStack();
    }
}