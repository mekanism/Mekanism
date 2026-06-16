package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class ChemicalCrystallizerRecipeProvider extends BaseSubRecipeProvider {

    ChemicalCrystallizerRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "crystallizing/";
        //Salt
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.BRINE, 15),
              MekanismItems.SALT.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "salt"));
        //Lithium
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.LITHIUM, 100),
              MekanismItems.LITHIUM_DUST.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "lithium"));
    }
}