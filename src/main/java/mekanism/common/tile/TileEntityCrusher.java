package mekanism.common.tile;

import java.util.Map;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.CrusherRecipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityCrusher extends TileEntityElectricMachine<CrusherRecipe> {

    public TileEntityCrusher() {
        super("crusher", "Crusher",
                MachineType.CRUSHER.getStorage(),
                MachineType.CRUSHER.getUsage(), 200);
    }

    @Override
    public Map<ItemStackInput, CrusherRecipe> getRecipes() {
        return Recipe.CRUSHER.get();
    }
}
