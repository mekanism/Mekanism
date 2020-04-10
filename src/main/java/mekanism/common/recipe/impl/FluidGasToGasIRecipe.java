package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class FluidGasToGasIRecipe extends FluidGasToGasRecipe {

    public FluidGasToGasIRecipe(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack output) {
        super(id, fluidInput, gasInput, output);
    }

    @Nonnull
    @Override
    public IRecipeType<FluidGasToGasRecipe> getType() {
        return MekanismRecipeType.WASHING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<FluidGasToGasRecipe> getSerializer() {
        return MekanismRecipeSerializers.WASHING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_WASHER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.CHEMICAL_WASHER.getItemStack();
    }
}