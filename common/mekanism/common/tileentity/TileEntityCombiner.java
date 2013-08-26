package mekanism.common.tileentity;

import java.util.Map;

import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class TileEntityCombiner extends TileEntityAdvancedElectricMachine
{
	public TileEntityCombiner()
	{
		super("Combiner.ogg", "Combiner", new ResourceLocation("mekanism", "gui/GuiCombiner.png"), Mekanism.combinerUsage, 1, 200, MachineType.COMBINER.baseEnergy, 200);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.COMBINER.get();
	}
	
	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		if(itemstack.getItem() instanceof ItemBlock && itemstack.itemID == Block.cobblestone.blockID)
		{
			return 200;
		}
		
		return 0;
	}
}
