package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class ChemicalCrystallizerRecipeProvider extends BaseSubRecipeProvider {

    ChemicalCrystallizerRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "crystallizing/";
        //Salt
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.BRINE, 15),
              MekanismItems.SALT.asTemplate()
        ).unlockedBy("in_brine", RecipeProvider.insideOf(MekanismFluids.BRINE.getBlock()))
              .unlockedBy("has_raw_meat", has(Tags.Items.FOODS_RAW_MEAT))
              .save(output, basePath + "salt");
        //Lithium
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.LITHIUM, 100),
              MekanismItems.LITHIUM_DUST.asTemplate()
        ).unlockedBy("in_lithium", RecipeProvider.insideOf(MekanismFluids.LITHIUM.getBlock()))
              .save(output, basePath + "lithium");
    }
}