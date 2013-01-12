package mekanism.nei;

import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import mekanism.client.GuiAdvancedElectricMachine;
import mekanism.common.TileEntityCombiner;

public class CombinerRecipeHandler extends AdvancedMachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Combiner";
	}

	@Override
	public String getRecipeId()
	{
		return "mekanism.combiner";
	}
	
	@Override
	public String getOverlayIdentifier()
	{
		return "combiner";
	}

	@Override
	public Set getRecipes()
	{
		return TileEntityCombiner.recipes.entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "/resources/mekanism/gui/GuiCombiner.png";
	}
	
	@Override
	public ItemStack getFuelStack()
	{
		return new ItemStack(Block.cobblestone);
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiAdvancedElectricMachine.class;
	}
}
