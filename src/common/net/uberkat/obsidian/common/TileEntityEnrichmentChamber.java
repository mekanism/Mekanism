package net.uberkat.obsidian.common;

import java.util.List;
import java.util.Vector;

import net.minecraft.src.*;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine
{
	public static List recipes = new Vector();
	
	public TileEntityEnrichmentChamber()
	{
		super("Chamber.ogg", "Enrichment Chamber", "/gui/GuiChamber.png", 5, 200, 1000);
	}
	
	public List getRecipes()
	{
		return recipes;
	}
}
