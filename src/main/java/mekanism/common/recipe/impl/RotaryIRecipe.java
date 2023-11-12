package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.serializer.RotaryRecipeSerializer.IFactory;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

@NothingNullByDefault
public class RotaryIRecipe extends BasicRotaryRecipe {

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
    public RecipeSerializer<BasicRotaryRecipe> getSerializer() {
        return MekanismRecipeSerializers.ROTARY.get();
    }

    public static class Factory implements IFactory<RotaryIRecipe> {

        @Override
        public RotaryIRecipe create(FluidStackIngredient fluidInput, GasStack gasOutput) {
            return new RotaryIRecipe(fluidInput, gasOutput);
        }

        @Override
        public RotaryIRecipe create(GasStackIngredient gasInput, FluidStack fluidOutput) {
            return new RotaryIRecipe(gasInput, fluidOutput);
        }

        @Override
        public RotaryIRecipe create(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput) {
            return new RotaryIRecipe(fluidInput, gasInput, gasOutput, fluidOutput);
        }
    }
}