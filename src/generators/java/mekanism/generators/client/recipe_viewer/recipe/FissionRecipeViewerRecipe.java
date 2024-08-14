package mekanism.generators.client.recipe_viewer.recipe;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributes.CooledCoolant;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.INamedRVRecipe;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.HeatUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import org.jetbrains.annotations.Nullable;

//If null -> coolant is water
public record FissionRecipeViewerRecipe(ResourceLocation id, @Nullable ChemicalStackIngredient inputCoolant, ChemicalStackIngredient fuel, ChemicalStack outputCoolant,
                                        ChemicalStack waste)
      implements INamedRVRecipe {

    public FluidStackIngredient waterInput() {
        return IngredientCreatorAccess.fluid().from(FluidTags.WATER, MathUtils.clampToInt(outputCoolant().getAmount()));
    }

    public static List<FissionRecipeViewerRecipe> getFissionRecipes() {
        //Note: The recipes below ignore thermal conductivity and just take enthalpy into account and it rounds the amount of coolant
        //TODO: Eventually we may want to try and improve on that but for now this should be fine
        List<FissionRecipeViewerRecipe> recipes = new ArrayList<>();
        long energyPerFuel = MekanismGeneratorsConfig.generators.energyPerFissionFuel.get();
        //Special case water recipe
        long coolantAmount = Math.round(energyPerFuel * HeatUtils.getSteamEnergyEfficiency() / HeatUtils.getWaterThermalEnthalpy());
        recipes.add(new FissionRecipeViewerRecipe(
              RecipeViewerUtils.synthetic(MekanismGenerators.rl("water"), "fission"),
              null, IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.FISSILE_FUEL, 1),
              MekanismChemicals.STEAM.getStack(coolantAmount), MekanismChemicals.NUCLEAR_WASTE.getStack(1)
        ));
        //Go through all gases and add each coolant
        for (Chemical chemical : MekanismAPI.CHEMICAL_REGISTRY) {
            CooledCoolant cooledCoolant = chemical.get(CooledCoolant.class);
            if (cooledCoolant != null) {
                //If it is a cooled coolant add a recipe for it
                Chemical heatedCoolant = cooledCoolant.getHeatedChemical();
                long amount = Math.round(energyPerFuel / cooledCoolant.getThermalEnthalpy());
                recipes.add(new FissionRecipeViewerRecipe(
                      RecipeViewerUtils.synthetic(chemical.getRegistryName(), "fission", MekanismGenerators.MODID),
                      IngredientCreatorAccess.chemicalStack().from(chemical, amount), IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.FISSILE_FUEL, 1),
                      heatedCoolant.getStack(amount), MekanismChemicals.NUCLEAR_WASTE.getStack(1)
                ));
            }
        }
        return recipes;
    }
}