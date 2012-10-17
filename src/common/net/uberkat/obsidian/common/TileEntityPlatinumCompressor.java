package net.uberkat.obsidian.common;

import java.util.List;
import java.util.Vector;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraft.src.*;
import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPlatinumCompressor extends TileEntityAdvancedElectricMachine
{
	public static List recipes = new Vector();
	
	public TileEntityPlatinumCompressor()
	{
		super("Platinum Compressor", "/gui/GuiCompressor.png", 5, 1, 200, 1000, 200);
	}
	
	public List getRecipes()
	{
		return recipes;
	}

	public int getFuelTicks(ItemStack itemstack)
	{
		if (itemstack.itemID == ObsidianIngots.PlatinumIngot.shiftedIndex) return 200;
		return 0;
	}
}
