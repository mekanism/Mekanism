package mekanism.generators.client.recipe_viewer.recipe;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.INamedRVRecipe;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.HeatUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import org.jetbrains.annotations.Nullable;

//If null -> coolant is water
public record FissionRecipeViewerRecipe(ResourceLocation id, @Nullable GasStackIngredient inputCoolant, GasStackIngredient fuel, GasStack outputCoolant, GasStack waste)
      implements INamedRVRecipe {

    public FluidStackIngredient waterInput() {
        return IngredientCreatorAccess.fluid().from(FluidTags.WATER, MathUtils.clampToInt(outputCoolant().getAmount()));
    }

    public static List<FissionRecipeViewerRecipe> getFissionRecipes() {
        //Note: The recipes below ignore thermal conductivity and just take enthalpy into account and it rounds the amount of coolant
        //TODO: Eventually we may want to try and improve on that but for now this should be fine
        List<FissionRecipeViewerRecipe> recipes = new ArrayList<>();
        double energyPerFuel = MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue();
        //Special case water recipe
        long coolantAmount = Math.round(energyPerFuel * HeatUtils.getSteamEnergyEfficiency() / HeatUtils.getWaterThermalEnthalpy());
        recipes.add(new FissionRecipeViewerRecipe(
              RecipeViewerUtils.synthetic(MekanismGenerators.rl("water"), "fission"),
              null, IngredientCreatorAccess.gasStack().from(MekanismGases.FISSILE_FUEL, 1),
              MekanismGases.STEAM.getStack(coolantAmount), MekanismGases.NUCLEAR_WASTE.getStack(1)
        ));
        //Go through all gases and add each coolant
        for (Gas gas : MekanismAPI.GAS_REGISTRY) {
            CooledCoolant cooledCoolant = gas.get(CooledCoolant.class);
            if (cooledCoolant != null) {
                //If it is a cooled coolant add a recipe for it
                Gas heatedCoolant = cooledCoolant.getHeatedGas();
                long amount = Math.round(energyPerFuel / cooledCoolant.getThermalEnthalpy());
                recipes.add(new FissionRecipeViewerRecipe(
                      RecipeViewerUtils.synthetic(gas.getRegistryName(), "fission", MekanismGenerators.MODID),
                      IngredientCreatorAccess.gasStack().from(gas, amount), IngredientCreatorAccess.gasStack().from(MekanismGases.FISSILE_FUEL, 1),
                      heatedCoolant.getStack(amount), MekanismGases.NUCLEAR_WASTE.getStack(1)
                ));
            }
        }
        return recipes;
    }
}