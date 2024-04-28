package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.MultiFluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.SingleFluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.TaggedFluidStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidInputCache<RECIPE extends MekanismRecipe> extends NBTSensitiveInputCache<Fluid, FluidStack, FluidStack, FluidStackIngredient, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, FluidStackIngredient inputIngredient) {
        switch (inputIngredient) {
            case SingleFluidStackIngredient single -> addNbtInputCache(single.getInputRaw(), recipe);
            case TaggedFluidStackIngredient tagged -> {
                for (Holder<Fluid> input : tagged.getRawInput()) {
                    addInputCache(input, recipe);
                }
            }
            case MultiFluidStackIngredient multi -> {
                return mapMultiInputs(recipe, multi);
            }
            default -> {
                //This should never really happen as we don't really allow for custom ingredients especially for networking,
                // but if it does add it as a fallback
                return true;
            }
        }
        return false;
    }

    @Override
    protected Fluid createKey(FluidStack stack) {
        return stack.getFluid();
    }

    @Override
    protected FluidStack createNbtKey(FluidStack stack) {
        //Note: We can use FluidStacks directly as the Nbt key as they compare only on fluid and tag on equals and hashcode
        // and don't take the amount into account
        return stack;
    }

    @Override
    public boolean isEmpty(FluidStack input) {
        return input.isEmpty();
    }
}