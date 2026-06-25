package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ElectrolysisRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class SeparatingRecipeProvider extends BaseSubRecipeProvider {

    SeparatingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "separator/";
        //Brine
        ElectrolysisRecipeBuilder.separating(
              IngredientCreatorAccess.fluid().from(this.fluids, MekanismTags.Fluids.BRINE, 10),
              chemicalTemplate(ChemicalIds.SODIUM, 1),
              chemicalTemplate(ChemicalIds.CHLORINE, 1)
        ).save(consumer, Mekanism.rl(basePath + "brine"));
        //Water
        ElectrolysisRecipeBuilder.separating(
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 2),
              chemicalTemplate(ChemicalIds.HYDROGEN, 2),
              chemicalTemplate(ChemicalIds.OXYGEN, 1)
        ).save(consumer, Mekanism.rl(basePath + "water"));
    }
}