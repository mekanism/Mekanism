package mekanism.common;

import java.util.Map;

import mekanism.common.BlockMachine.MachineType;
import mekanism.common.RecipeHandler.Recipe;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine
{
	public TileEntityEnrichmentChamber()
	{
		super("Chamber.ogg", "Enrichment Chamber", "/mods/mekanism/gui/GuiChamber.png", 50, 200, MachineType.ENRICHMENT_CHAMBER.baseEnergy);
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
}
