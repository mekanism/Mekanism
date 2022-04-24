package mekanism.generators.client.jei;

import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.generators.client.jei.recipe.FissionJEIRecipe;
import mekanism.generators.common.MekanismGenerators;

public class GeneratorsJEIRecipeType {

    public static final MekanismJEIRecipeType<FissionJEIRecipe> FISSION = new MekanismJEIRecipeType<>(MekanismGenerators.rl("fission"), FissionJEIRecipe.class);
}