package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

@NothingNullByDefault
public class FluidToFluidIRecipe extends BasicFluidToFluidRecipe {

    public FluidToFluidIRecipe(FluidStackIngredient input, FluidStack output) {
        super(input, output);
    }

    @Override
    public RecipeSerializer<FluidToFluidIRecipe> getSerializer() {
        return MekanismRecipeSerializers.EVAPORATING.get();
    }

    public FluidStack getOutputRaw() {
        return output;
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getItemStack();
    }
}