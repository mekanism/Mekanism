package mekanism.common;

import java.util.Map;

import mekanism.common.RecipeHandler.Recipe;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine
{
	public TileEntityEnrichmentChamber()
	{
		super("Chamber.ogg", "Enrichment Chamber", "/mods/mekanism/gui/GuiChamber.png", 10, 200, 2000);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.ENRICHMENT_CHAMBER.get();
	}
}
