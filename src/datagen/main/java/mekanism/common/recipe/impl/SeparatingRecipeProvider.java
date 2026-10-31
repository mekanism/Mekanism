package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ElectrolysisRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

class SeparatingRecipeProvider extends BaseSubRecipeProvider {

    SeparatingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "separator/";
        //Brine
        ElectrolysisRecipeBuilder.separating(
              IngredientCreatorAccess.fluid().from(this.fluids, MekanismTags.Fluids.BRINE, 10),
              chemicalTemplate(ChemicalIds.SODIUM, 1),
              chemicalTemplate(ChemicalIds.CHLORINE, 1)
        ).unlockedBy("has_salt", has(MekanismTags.Items.DUSTS_SALT))
              .unlockedBy("in_brine", RecipeProvider.insideOf(MekanismFluids.BRINE.getBlock()))
              .save(output, basePath + "brine");
        //Water
        ElectrolysisRecipeBuilder.separating(
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 2),
              chemicalTemplate(ChemicalIds.HYDROGEN, 2),
              chemicalTemplate(ChemicalIds.OXYGEN, 1)
        ).unlockedBy("in_water", RecipeProvider.insideOf(Blocks.WATER))
              .save(output, basePath + "water");
    }
}