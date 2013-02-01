package mekanism.common;

import java.util.HashMap;
import java.util.Map;

import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.item.ItemStack;

public class TileEntityCrusher extends TileEntityElectricMachine
{
	public TileEntityCrusher()
	{
		super("Crusher.ogg", "Crusher", "/resources/mekanism/gui/GuiCrusher.png", 16, 200, 3200);
	}
	
	@Override
	public HashMap getRecipes()
	{
		return Recipe.CRUSHER.get();
	}
}
