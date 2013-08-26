package mekanism.client.nei;

import java.util.Set;

import net.minecraft.item.ItemStack;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler.Recipe;

public class OsmiumCompressorRecipeHandler extends AdvancedMachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Osmium Compressor";
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
		return Recipe.OSMIUM_COMPRESSOR.get().entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiCompressor.png";
	}
	
	@Override
	public ItemStack getFuelStack()
	{
		return new ItemStack(Mekanism.Ingot, 1, 1);
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiOsmiumCompressor.class;
	}
}
