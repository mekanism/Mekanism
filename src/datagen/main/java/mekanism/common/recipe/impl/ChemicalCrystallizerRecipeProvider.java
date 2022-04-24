package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import net.minecraft.data.recipes.FinishedRecipe;

class ChemicalCrystallizerRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "crystallizing/";
        //Salt
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.gas().from(MekanismGases.BRINE, 15),
              MekanismItems.SALT.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "salt"));
        //Lithium
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.gas().from(MekanismGases.LITHIUM, 100),
              MekanismItems.LITHIUM_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "lithium"));
    }
}