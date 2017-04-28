package mekanism.common.tile;

import java.util.Map;

import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine<EnrichmentRecipe>
{
	public TileEntityEnrichmentChamber()
	{
		super("enrichment", "EnrichmentChamber", 200, BlockStateMachine.MachineType.ENRICHMENT_CHAMBER.baseEnergy, usage.enrichmentChamberUsage);
	}

	@Override
	public Map getRecipes()
	{
		return Recipe.ENRICHMENT_CHAMBER.get();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getVolume()
	{
		return 0.3F*super.getVolume();
	}
}
