package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.serializer.RotaryRecipeSerializer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

@NothingNullByDefault
public class RotaryIRecipe extends RotaryRecipe {

    public RotaryIRecipe(FluidStackIngredient fluidInput, GasStack gasOutput) {
        super(fluidInput, gasOutput);
    }

    public RotaryIRecipe(GasStackIngredient gasInput, FluidStack fluidOutput) {
        super(gasInput, fluidOutput);
    }

    public RotaryIRecipe(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput) {
        super(fluidInput, gasInput, gasOutput, fluidOutput);
    }

    @Override
    public RecipeType<RotaryRecipe> getType() {
        return MekanismRecipeType.ROTARY.get();
    }

    @Override
    public RecipeSerializer<RotaryRecipe> getSerializer() {
        return MekanismRecipeSerializers.ROTARY.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.ROTARY_CONDENSENTRATOR.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.ROTARY_CONDENSENTRATOR.getItemStack();
    }

    public static class Factory implements RotaryRecipeSerializer.IFactory<RotaryRecipe> {

        @Override
        public RotaryRecipe create(FluidStackIngredient fluidInput, GasStack gasOutput) {
            return new RotaryIRecipe(fluidInput, gasOutput);
        }

        @Override
        public RotaryRecipe create(GasStackIngredient gasInput, FluidStack fluidOutput) {
            return new RotaryIRecipe(gasInput, fluidOutput);
        }

        @Override
        public RotaryRecipe create(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput) {
            return new RotaryIRecipe(fluidInput, gasInput, gasOutput, fluidOutput);
        }
    }
}