package mekanism.nei;

import java.util.Set;

import net.minecraft.item.ItemStack;

import mekanism.client.GuiAdvancedElectricMachine;
import mekanism.common.Mekanism;
import mekanism.common.TileEntityPlatinumCompressor;

public class PlatinumCompressorRecipeHandler extends AdvancedMachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Platinum Compressor";
	}

	@Override
	public String getRecipeId()
	{
		return "mekanism.compressor";
	}
	
	@Override
	public String getOverlayIdentifier()
	{
		return "compressor";
	}

	@Override
	public Set getRecipes()
	{
		return TileEntityPlatinumCompressor.recipes.entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "/resources/mekanism/gui/GuiCompressor.png";
	}
	
	@Override
	public ItemStack getFuelStack()
	{
		return new ItemStack(Mekanism.Ingot, 1, 1);
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiAdvancedElectricMachine.class;
	}
}
