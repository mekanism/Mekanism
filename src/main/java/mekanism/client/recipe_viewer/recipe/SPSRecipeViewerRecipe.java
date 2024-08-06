package mekanism.client.recipe_viewer.recipe;

import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.INamedRVRecipe;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismChemicals;
import net.minecraft.resources.ResourceLocation;

//TODO - V11: Make the SPS have a proper recipe type to allow for custom recipes
public record SPSRecipeViewerRecipe(ResourceLocation id, ChemicalStackIngredient input, ChemicalStack output) implements INamedRVRecipe {

    public static List<SPSRecipeViewerRecipe> getSPSRecipes() {
        return Collections.singletonList(new SPSRecipeViewerRecipe(
              RecipeViewerUtils.synthetic(Mekanism.rl("antimatter"), "sps"),
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.POLONIUM, MekanismConfig.general.spsInputPerAntimatter.get()),
              MekanismChemicals.ANTIMATTER.getStack(1)
        ));
    }
}