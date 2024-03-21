package mekanism.client.recipe_viewer.recipe;

import java.util.Map;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismGases;
import net.minecraft.resources.ResourceLocation;

//TODO - V11: Make the SPS have a proper recipe type to allow for custom recipes
public record SPSRecipeViewerRecipe(GasStackIngredient input, GasStack output) {

    public static Map<ResourceLocation, SPSRecipeViewerRecipe> getSPSRecipes() {
        return Map.of(Mekanism.rl("generated_antimatter"), new SPSRecipeViewerRecipe(IngredientCreatorAccess.gas().from(MekanismGases.POLONIUM, MekanismConfig.general.spsInputPerAntimatter.get()),
              MekanismGases.ANTIMATTER.getStack(1)));
    }
}