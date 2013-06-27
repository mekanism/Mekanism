package mekanism.common;

import java.util.Map;

import mekanism.common.BlockMachine.MachineType;
import mekanism.common.RecipeHandler.Recipe;

public class TileEntityCrusher extends TileEntityElectricMachine
{
	public TileEntityCrusher()
	{
		super("Crusher.ogg", "Crusher", "/mods/mekanism/gui/GuiCrusher.png", Mekanism.crusherUsage, 200, MachineType.CRUSHER.baseEnergy);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.CRUSHER.get();
	}
}
