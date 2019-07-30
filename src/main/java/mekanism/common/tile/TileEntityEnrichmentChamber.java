package mekanism.common.tile;

import java.util.Map;
import mekanism.common.block.states.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine<EnrichmentRecipe> {

    public TileEntityEnrichmentChamber() {
        super("enrichment", MachineType.ENRICHMENT_CHAMBER, 200);
    }

    @Override
    public Map<ItemStackInput, EnrichmentRecipe> getRecipes() {
        return Recipe.ENRICHMENT_CHAMBER.get();
    }
}