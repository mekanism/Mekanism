package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class ElectrolysisIRecipe extends ElectrolysisRecipe {

    public ElectrolysisIRecipe(ResourceLocation id, FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput) {
        super(id, input, energyMultiplier, leftGasOutput, rightGasOutput);
    }

    @Nonnull
    @Override
    public IRecipeType<ElectrolysisRecipe> getType() {
        return MekanismRecipeType.SEPARATING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ElectrolysisRecipe> getSerializer() {
        return MekanismRecipeSerializers.SEPARATING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.ELECTROLYTIC_SEPARATOR.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.ELECTROLYTIC_SEPARATOR.getItemStack();
    }
}