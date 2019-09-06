package mekanism.common.tile;

import java.util.List;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.prefab.TileEntityChanceMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class TileEntityPrecisionSawmill extends TileEntityChanceMachine<SawmillRecipe> {

    public TileEntityPrecisionSawmill() {
        super("sawmill", MachineType.PRECISION_SAWMILL, 200, MekanismUtils.getResource(ResourceType.GUI, "GuiBasicMachine.png"));
    }

    @Override
    public List<SawmillRecipe> getRecipes() {
        return Recipe.PRECISION_SAWMILL.get();
    }
}