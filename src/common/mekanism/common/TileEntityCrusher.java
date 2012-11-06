package mekanism.common;

import java.util.List;
import java.util.Vector;

import net.minecraft.src.*;

public class TileEntityCrusher extends TileEntityElectricMachine
{
	public static List recipes = new Vector();
	
	public TileEntityCrusher()
	{
		super("Crusher.ogg", "Crusher", "/gui/GuiCrusher.png", 5, 200, 1000);
	}
	
	@Override
	public List getRecipes()
	{
		return recipes;
	}
}
