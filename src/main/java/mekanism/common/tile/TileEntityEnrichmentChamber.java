package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine {

    public TileEntityEnrichmentChamber() {
        super("enrichment", MachineType.ENRICHMENT_CHAMBER, 200);
    }

    @Nonnull
    @Override
    public Recipe<ItemStackToItemStackRecipe> getRecipes() {
        return Recipe.ENRICHMENT_CHAMBER;
    }
}