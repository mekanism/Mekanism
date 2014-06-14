package mekanism.client.nei;

import java.util.Set;

import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiSlot;
import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.client.gui.GuiSlot.SlotType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class CrusherRecipeHandler extends MachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock.Crusher.name");
	}

	@Override
	public String getRecipeId()
	{
		return "mekanism.crusher";
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "crusher";
	}
	
	@Override
	public ProgressBar getProgressType()
	{
		return ProgressBar.CRUSH;
	}

	@Override
	public Set getRecipes()
	{
		return Recipe.CRUSHER.get().entrySet();
	}

	@Override
	public Class getGuiClass()
	{
		return GuiCrusher.class;
	}
}
