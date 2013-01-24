package mekanism.common;

import java.util.HashMap;
import java.util.Map;

import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.item.ItemStack;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine
{
	public TileEntityEnrichmentChamber()
	{
		super("Chamber.ogg", "Enrichment Chamber", "/resources/mekanism/gui/GuiChamber.png", 16, 200, 1000);
	}
	
	@Override
	public HashMap getRecipes()
	{
		return Recipe.ENRICHMENT_CHAMBER.get();
	}
}
