package mekanism.common.tile;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.prefab.TileEntityDoubleElectricMachine;

public class TileEntityCombiner extends TileEntityDoubleElectricMachine<CombinerRecipe> {

    public TileEntityCombiner() {
        super("combiner", MachineType.COMBINER, 200);
    }

    @Nonnull
    @Override
    public Recipe<CombinerRecipe> getRecipes() {
        return Recipe.COMBINER;
    }
}