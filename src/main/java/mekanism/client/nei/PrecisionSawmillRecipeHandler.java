package mekanism.client.nei;

import java.util.Collection;

import mekanism.client.gui.GuiPrecisionSawmill;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.util.MekanismUtils;

public class PrecisionSawmillRecipeHandler extends ChanceMachineRecipeHandler
{	
	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock2.PrecisionSawmill.name");
	}

	@Override
	public String getRecipeId()
	{
		return "mekanism.precisionsawmill";
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "precisionsawmill";
	}

	@Override
	public Collection<SawmillRecipe> getRecipes()
	{
		return Recipe.PRECISION_SAWMILL.get().values();
	}
	
	@Override
	public ProgressBar getProgressType()
	{
		return ProgressBar.PURPLE;
	}

	@Override
	public Class getGuiClass()
	{
		return GuiPrecisionSawmill.class;
	}
}
