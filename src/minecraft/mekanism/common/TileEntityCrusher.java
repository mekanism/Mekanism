package mekanism.common;

import java.util.HashMap;
import java.util.Map;

import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.item.ItemStack;

public class TileEntityCrusher extends TileEntityElectricMachine
{
	public TileEntityCrusher()
	{
		super("Crusher.ogg", "Crusher", "/mods/mekanism/gui/GuiCrusher.png", 10, 200, 2000);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.CRUSHER.get();
	}
}
