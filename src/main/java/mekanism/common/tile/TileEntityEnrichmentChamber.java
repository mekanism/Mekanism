package mekanism.common.tile;

import java.util.Map;

import mekanism.api.MekanismConfig.usage;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.EnrichmentRecipe;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine<EnrichmentRecipe>
{
	public TileEntityEnrichmentChamber()
	{
		super("enrichment", "EnrichmentChamber", usage.enrichmentChamberUsage, 200, MachineBlockType.ENRICHMENT_CHAMBER.baseEnergy);
	}

	@Override
	public Map getRecipes()
	{
		return Recipe.ENRICHMENT_CHAMBER.get();
	}

	@Override
	public float getVolume()
	{
		return 0.3F;
	}
}
