package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.FluidToFluidRecipeBuilder;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.tags.FluidTags;

class EvaporatingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "evaporating/";
        //Brine
        FluidToFluidRecipeBuilder.evaporating(
              FluidStackIngredient.from(FluidTags.WATER, 10),
              MekanismFluids.BRINE.getFluidStack(1)
        ).build(consumer, Mekanism.rl(basePath + "brine"));
        //Lithium
        FluidToFluidRecipeBuilder.evaporating(
              FluidStackIngredient.from(MekanismTags.Fluids.BRINE, 10),
              MekanismFluids.LITHIUM.getFluidStack(1)
        ).build(consumer, Mekanism.rl(basePath + "lithium"));
    }
}