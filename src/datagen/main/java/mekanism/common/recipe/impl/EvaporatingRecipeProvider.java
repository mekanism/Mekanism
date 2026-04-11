package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.FluidToFluidRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;

class EvaporatingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "evaporating/";
        RegistryLookup<Fluid> holderGetter = registries.lookupOrThrow(Registries.FLUID);
        //Brine
        FluidToFluidRecipeBuilder.evaporating(
              IngredientCreatorAccess.fluid().from(holderGetter, FluidTags.WATER, 10),
              MekanismFluids.BRINE.asStack(1)
        ).save(consumer, Mekanism.rl(basePath + "brine"));
        //Lithium
        FluidToFluidRecipeBuilder.evaporating(
              IngredientCreatorAccess.fluid().from(holderGetter, MekanismTags.Fluids.BRINE, 10),
              MekanismFluids.LITHIUM.asStack(1)
        ).save(consumer, Mekanism.rl(basePath + "lithium"));
    }
}