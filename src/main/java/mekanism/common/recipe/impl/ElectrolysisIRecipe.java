package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ElectrolysisIRecipe extends ElectrolysisRecipe {

    public ElectrolysisIRecipe(ResourceLocation id, FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput) {
        super(id, input, energyMultiplier, leftGasOutput, rightGasOutput);
    }

    @Nonnull
    @Override
    public RecipeType<ElectrolysisRecipe> getType() {
        return MekanismRecipeType.SEPARATING.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<ElectrolysisRecipe> getSerializer() {
        return MekanismRecipeSerializers.SEPARATING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.ELECTROLYTIC_SEPARATOR.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.ELECTROLYTIC_SEPARATOR.getItemStack();
    }
}