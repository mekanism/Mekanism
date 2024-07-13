package mekanism.client.recipe_viewer.recipe;

import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.INamedRVRecipe;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismGases;
import net.minecraft.resources.ResourceLocation;

//TODO - V11: Make the SPS have a proper recipe type to allow for custom recipes
public record SPSRecipeViewerRecipe(ResourceLocation id, GasStackIngredient input, GasStack output) implements INamedRVRecipe {

    public static List<SPSRecipeViewerRecipe> getSPSRecipes() {
        return Collections.singletonList(new SPSRecipeViewerRecipe(
              RecipeViewerUtils.synthetic(Mekanism.rl("antimatter"), "sps"),
              IngredientCreatorAccess.gasStack().from(MekanismGases.POLONIUM, MekanismConfig.general.spsInputPerAntimatter.get()),
              MekanismGases.ANTIMATTER.getStack(1)
        ));
    }
}