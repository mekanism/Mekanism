package mekanism.generators.client.recipe_viewer;

import mekanism.client.recipe_viewer.type.FakeRVRecipeType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.recipe_viewer.recipe.FissionRecipeViewerRecipe;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class GeneratorsRVRecipeType {

    public static final FakeRVRecipeType<FissionRecipeViewerRecipe> FISSION = new FakeRVRecipeType<>(MekanismGenerators.rl("fission"), MekanismUtils.getResource(ResourceType.GUI, "radioactive.png"), GeneratorsLang.FISSION_REACTOR, FissionRecipeViewerRecipe.class, -6, -13, 182, 60,
          GeneratorsBlocks.FISSION_REACTOR_CASING, GeneratorsBlocks.FISSION_REACTOR_PORT, GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER, GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, GeneratorsBlocks.CONTROL_ROD_ASSEMBLY);
}