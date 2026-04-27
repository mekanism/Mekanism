package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.ElectrolysisRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;

class SeparatingRecipeProvider implements ISubRecipeProvider {

    private final HolderGetter<Fluid> fluids;

    public SeparatingRecipeProvider(HolderGetter<Fluid> fluids) {
        this.fluids = fluids;
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "separator/";
        //Brine
        ElectrolysisRecipeBuilder.separating(
              IngredientCreatorAccess.fluid().from(this.fluids, MekanismTags.Fluids.BRINE, 10),
              MekanismChemicals.SODIUM.asStack(1),
              MekanismChemicals.CHLORINE.asStack(1)
        ).save(consumer, Mekanism.rl(basePath + "brine"));
        //Water
        ElectrolysisRecipeBuilder.separating(
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 2),
              MekanismChemicals.HYDROGEN.asStack(2),
              MekanismChemicals.OXYGEN.asStack(1)
        ).save(consumer, Mekanism.rl(basePath + "water"));
    }
}