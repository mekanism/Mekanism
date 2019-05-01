package mekanism.common.tile;

import java.util.Map;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.tile.prefab.TileEntityDoubleElectricMachine;

public class TileEntityCombiner extends TileEntityDoubleElectricMachine<CombinerRecipe> {

    public TileEntityCombiner() {
        super("combiner", "Combiner",
                MachineType.COMBINER.getStorage(),
                MachineType.COMBINER.getUsage(), 200);
    }

    @Override
    public Map<DoubleMachineInput, CombinerRecipe> getRecipes() {
        return Recipe.COMBINER.get();
    }
}
