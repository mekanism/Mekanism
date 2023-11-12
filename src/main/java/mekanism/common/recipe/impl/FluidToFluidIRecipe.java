package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

@NothingNullByDefault
public class FluidToFluidIRecipe extends BasicFluidToFluidRecipe {

    public FluidToFluidIRecipe(FluidStackIngredient input, FluidStack output) {
        super(input, output);
    }

    @Override
    public RecipeSerializer<BasicFluidToFluidRecipe> getSerializer() {
        return MekanismRecipeSerializers.EVAPORATING.get();
    }

}