package mekanism.client.recipe_viewer.recipe;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.INamedRVRecipe;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.HeatUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import org.jetbrains.annotations.Nullable;

public record BoilerRecipeViewerRecipe(ResourceLocation id, @Nullable GasStackIngredient superHeatedCoolant, FluidStackIngredient water, GasStack steam,
                                       GasStack cooledCoolant, double temperature) implements INamedRVRecipe {

    public static List<BoilerRecipeViewerRecipe> getBoilerRecipes() {
        //Note: The recipes below ignore the boiler's efficiency and rounds the amount of coolant
        int waterAmount = 1;
        double waterToSteamEfficiency = HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
        List<BoilerRecipeViewerRecipe> recipes = new ArrayList<>();
        //Special case heat only recipe
        double temperature = waterAmount * waterToSteamEfficiency / (BoilerMultiblockData.CASING_HEAT_CAPACITY * MekanismConfig.general.boilerWaterConductivity.get()) +
                             HeatUtils.BASE_BOIL_TEMP;
        recipes.add(new BoilerRecipeViewerRecipe(
              RecipeViewerUtils.synthetic(Mekanism.rl("water"), "boiler"),
              null, IngredientCreatorAccess.fluid().from(FluidTags.WATER, waterAmount),
              MekanismGases.STEAM.getStack(waterAmount), GasStack.EMPTY,
              temperature
        ));
        //Go through all gases and add each coolant
        for (Gas gas : MekanismAPI.GAS_REGISTRY) {
            HeatedCoolant heatedCoolant = gas.get(HeatedCoolant.class);
            if (heatedCoolant != null) {
                //If it is a cooled coolant add a recipe for it
                Gas cooledCoolant = heatedCoolant.getCooledGas();
                long coolantAmount = Math.round(waterAmount * waterToSteamEfficiency / heatedCoolant.getThermalEnthalpy());
                recipes.add(new BoilerRecipeViewerRecipe(
                      RecipeViewerUtils.synthetic(gas.getRegistryName(), "boiler", Mekanism.MODID),
                      IngredientCreatorAccess.gasStack().from(gas, coolantAmount), IngredientCreatorAccess.fluid().from(FluidTags.WATER, waterAmount),
                      MekanismGases.STEAM.getStack(waterAmount), cooledCoolant.getStack(coolantAmount),
                      HeatUtils.BASE_BOIL_TEMP
                ));
            }
        }
        return recipes;
    }
}