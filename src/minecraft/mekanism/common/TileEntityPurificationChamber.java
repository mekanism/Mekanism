package mekanism.common;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mekanism.common.RecipeHandler.Recipe;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine
{
	public TileEntityPurificationChamber()
	{
		super("PurificationChamber.ogg", "Purification Chamber", "/resources/mekanism/gui/GuiPurificationChamber.png", 20, 1, 200, 12000, 1200);
	}
	
	@Override
	public HashMap getRecipes()
	{
		return Recipe.PURIFICATION_CHAMBER.get();
	}
	
	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		if(itemstack.isItemEqual(new ItemStack(Block.sand))) return 10;
		if(itemstack.isItemEqual(new ItemStack(Item.flint))) return 300;
		return 0;
	}
}
