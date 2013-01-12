package mekanism.nei;

import java.util.Set;

import mekanism.client.GuiElectricMachine;
import mekanism.common.TileEntityCrusher;

public class CrusherRecipeHandler extends MachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Crusher";
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
		return TileEntityCrusher.recipes.entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "/resources/mekanism/gui/GuiCrusher.png";
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiElectricMachine.class;
	}
}
