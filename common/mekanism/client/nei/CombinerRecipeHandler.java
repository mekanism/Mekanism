package mekanism.client.nei;

import java.util.List;
import java.util.Set;

import mekanism.api.ListUtils;
import mekanism.api.gas.Gas;
import mekanism.client.gui.GuiCombiner;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class CombinerRecipeHandler extends AdvancedMachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock.Combiner.name");
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
		return Recipe.COMBINER.get().entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiCombiner.png";
	}

	@Override
	public List<ItemStack> getFuelStacks(Gas gasType)
	{
		return ListUtils.asList(new ItemStack(Block.cobblestone));
	}

	@Override
	public Class getGuiClass()
	{
		return GuiCombiner.class;
	}
}
