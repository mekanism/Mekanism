package mekanism.client.nei;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import mekanism.client.gui.GuiCombiner;
import mekanism.common.RecipeHandler.Recipe;

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
		return Recipe.COMBINER.get().entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiCombiner.png";
	}
	
	@Override
	public ItemStack getFuelStack()
	{
		return new ItemStack(Block.cobblestone);
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiCombiner.class;
	}
}
