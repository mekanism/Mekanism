package mekanism.client.nei;

import java.util.Set;

import mekanism.client.gui.GuiCrusher;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;

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
	public Set getRecipes()
	{
		return Recipe.CRUSHER.get().entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiCrusher.png";
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiCrusher.class;
	}
}
