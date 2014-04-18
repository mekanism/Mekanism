package mekanism.common.tile;

import java.util.Map;

import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import net.minecraft.util.ResourceLocation;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine
{
	public TileEntityEnrichmentChamber()
	{
		super("Chamber.ogg", "EnrichmentChamber", Mekanism.enrichmentChamberUsage, 200, MachineType.ENRICHMENT_CHAMBER.baseEnergy);
	}

	@Override
	public Map getRecipes()
	{
		return Recipe.ENRICHMENT_CHAMBER.get();
	}

	@Override
	public float getVolumeMultiplier()
	{
		return 0.3F;
	}

	@Override
	public ProgressBar getProgressType()
	{
		return ProgressBar.BLUE;
	}
}
