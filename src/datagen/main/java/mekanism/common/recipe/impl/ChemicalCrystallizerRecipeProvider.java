package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.GasToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import net.minecraft.data.IFinishedRecipe;

class ChemicalCrystallizerRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "crystallizing/";
        //Salt
        GasToItemStackRecipeBuilder.crystallizing(
              GasStackIngredient.from(MekanismGases.BRINE, 15),
              MekanismItems.SALT.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "salt"));
        //Lithium
        GasToItemStackRecipeBuilder.crystallizing(
              GasStackIngredient.from(MekanismGases.LITHIUM, 100),
              MekanismItems.LITHIUM_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "lithium"));
    }
}