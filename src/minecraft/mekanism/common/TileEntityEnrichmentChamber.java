package mekanism.common;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine
{
	public static Map<ItemStack, ItemStack> recipes = new HashMap<ItemStack, ItemStack>();
	
	public TileEntityEnrichmentChamber()
	{
		super("Chamber.ogg", "Enrichment Chamber", "/resources/mekanism/gui/GuiChamber.png", 5, 200, 1000);
	}
	
	@Override
	public Map getRecipes()
	{
		return recipes;
	}
}
