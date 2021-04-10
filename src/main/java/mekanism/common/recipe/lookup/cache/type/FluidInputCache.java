package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.lib.HashedFluid;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidInputCache<RECIPE extends MekanismRecipe> extends NBTSensitiveInputCache<Fluid, HashedFluid, FluidStack, FluidStackIngredient, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, FluidStackIngredient inputIngredient) {
        if (inputIngredient instanceof FluidStackIngredient.Single) {
            HashedFluid input = HashedFluid.create(((FluidStackIngredient.Single) inputIngredient).getInputRaw());
            addNbtInputCache(input, recipe);
        } else if (inputIngredient instanceof FluidStackIngredient.Tagged) {
            for (Fluid input : ((FluidStackIngredient.Tagged) inputIngredient).getRawInput()) {
                addInputCache(input, recipe);
            }
        } else if (inputIngredient instanceof FluidStackIngredient.Multi) {
            return ((FluidStackIngredient.Multi) inputIngredient).forEachIngredient(ingredient -> mapInputs(recipe, ingredient));
        } else {
            //This should never really happen as we don't really allow for custom ingredients especially for networking,
            // but if it does add it as a fallback
            return true;
        }
        return false;
    }

    @Override
    protected Fluid createKey(FluidStack stack) {
        return stack.getFluid();
    }

    @Override
    protected HashedFluid createNbtKey(FluidStack stack) {
        return HashedFluid.raw(stack);
    }

    @Override
    public boolean isEmpty(FluidStack input) {
        return input.isEmpty();
    }
}