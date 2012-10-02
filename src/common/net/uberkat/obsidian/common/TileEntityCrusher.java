package net.uberkat.obsidian.common;

import java.util.List;
import java.util.Vector;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.minecraft.src.*;

public class TileEntityCrusher extends TileEntityElectricMachine
{
	public static List recipes = new Vector();
	
	public TileEntityCrusher()
	{
		super("Crusher", 5, 200, 1000);
	}
	
	public List getRecipes()
	{
		return recipes;
	}
}
