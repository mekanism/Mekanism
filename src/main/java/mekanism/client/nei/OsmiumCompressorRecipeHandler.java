package mekanism.client.nei;

import java.util.Collection;
import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.MekanismItems;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;

public class OsmiumCompressorRecipeHandler extends AdvancedMachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock.OsmiumCompressor.name");
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
	public Collection<OsmiumCompressorRecipe> getRecipes()
	{
		return Recipe.OSMIUM_COMPRESSOR.get().values();
	}
	
	@Override
	public ProgressBar getProgressType()
	{
		return ProgressBar.RED;
	}

	@Override
	public List<ItemStack> getFuelStacks(Gas gasType)
	{
		return ListUtils.asList(new ItemStack(MekanismItems.Ingot, 1, 1));
	}

	@Override
	public Class getGuiClass()
	{
		return GuiOsmiumCompressor.class;
	}
}
