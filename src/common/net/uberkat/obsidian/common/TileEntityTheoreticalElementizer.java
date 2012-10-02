package net.uberkat.obsidian.common;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.common.ForgeDirection;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.*;

public class TileEntityTheoreticalElementizer extends TileEntityAdvancedElectricMachine
{
	public static List recipes = new Vector();
	
	public TileEntityTheoreticalElementizer()
	{
		super("Theoretical Elementizer", 50, 1, 1000, 10000, 1000);
	}
	
	public List getRecipes()
	{
		return recipes;
	}

	public int getFuelTicks(ItemStack itemstack)
	{
		if (itemstack.itemID == Item.diamond.shiftedIndex) return 1000;
		return 0;
	}
	
    public static Item getRandomMagicItem()
    {
    	Random rand = new Random();
    	int random = rand.nextInt(3);
    	if(random == 0) return ObsidianIngots.LightningRod;
    	if(random == 1) return ObsidianIngots.Stopwatch;
    	if(random == 2) return ObsidianIngots.WeatherOrb;
    	return ObsidianIngots.EnrichedAlloy;
    }
}
