package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.FluidToFluidRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class EvaporatingRecipeProvider extends BaseSubRecipeProvider {

    EvaporatingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "evaporating/";
        //Brine
        FluidToFluidRecipeBuilder.evaporating(
              IngredientCreatorAccess.fluid().from(fluids, FluidTags.WATER, 10),
              MekanismFluids.BRINE.asTemplate(1)
        ).save(consumer, Mekanism.rl(basePath + "brine"));
        //Lithium
        FluidToFluidRecipeBuilder.evaporating(
              IngredientCreatorAccess.fluid().from(fluids, MekanismTags.Fluids.BRINE, 10),
              MekanismFluids.LITHIUM.asTemplate(1)
        ).save(consumer, Mekanism.rl(basePath + "lithium"));
    }
}