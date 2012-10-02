package net.uberkat.obsidian.common;

import java.util.List;
import java.util.Vector;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.common.ForgeDirection;
import net.minecraft.src.*;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine
{
	public static List recipes = new Vector();
	
	public TileEntityEnrichmentChamber()
	{
		super("Enrichment Chamber", 5, 200, 1000);
	}
	
	public List getRecipes()
	{
		return recipes;
	}
}
