package mekanism.common.tile;

import java.util.List;
import mekanism.api.recipes.ItemStack2ItemStackRecipe;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityCrusher extends TileEntityElectricMachine<ItemStack2ItemStackRecipe> {

    public TileEntityCrusher() {
        super("crusher", MachineType.CRUSHER, 200);
    }

    @Override
    public List<ItemStack2ItemStackRecipe> getRecipes() {
        return Recipe.CRUSHER.get();
    }
}