package mekanism.common;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

public class TileEntityCrusher extends TileEntityElectricMachine
{
	public static Map<ItemStack, ItemStack> recipes = new HashMap<ItemStack, ItemStack>();
	
	public TileEntityCrusher()
	{
		super("Crusher.ogg", "Crusher", "/resources/mekanism/gui/GuiCrusher.png", 5, 200, 1000);
	}
	
	@Override
	public Map getRecipes()
	{
		return recipes;
	}
}
