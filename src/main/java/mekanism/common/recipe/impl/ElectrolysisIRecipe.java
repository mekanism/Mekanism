package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
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

@NothingNullByDefault
public class ElectrolysisIRecipe extends ElectrolysisRecipe {

    public ElectrolysisIRecipe(ResourceLocation id, FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput) {
        super(id, input, energyMultiplier, leftGasOutput, rightGasOutput);
    }

    @Override
    public RecipeType<ElectrolysisRecipe> getType() {
        return MekanismRecipeType.SEPARATING.get();
    }

    @Override
    public RecipeSerializer<ElectrolysisRecipe> getSerializer() {
        return MekanismRecipeSerializers.SEPARATING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.ELECTROLYTIC_SEPARATOR.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.ELECTROLYTIC_SEPARATOR.getItemStack();
    }
}