package mekanism.common.tile;

import java.util.Map;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine<EnrichmentRecipe> {

    public TileEntityEnrichmentChamber() {
        super("enrichment", "EnrichmentChamber", BlockStateMachine.MachineType.ENRICHMENT_CHAMBER.baseEnergy,
              usage.enrichmentChamberUsage, 200);
    }

    @Override
    public Map<ItemStackInput, EnrichmentRecipe> getRecipes() {
        return Recipe.ENRICHMENT_CHAMBER.get();
    }
}
