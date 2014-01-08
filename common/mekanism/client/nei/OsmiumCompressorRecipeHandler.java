package mekanism.client.nei;

import java.util.List;
import java.util.Set;

import mekanism.api.ListUtils;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeHandler.Recipe;
import net.minecraft.item.ItemStack;

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
	public List<ItemStack> getFuelStacks()
	{
		return ListUtils.asList(new ItemStack(Mekanism.Ingot, 1, 1), new ItemStack(Mekanism.BasicBlock, 1, 0));
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiOsmiumCompressor.class;
	}
}
