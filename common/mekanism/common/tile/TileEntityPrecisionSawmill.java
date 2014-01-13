package mekanism.common.tile;

import java.util.Map;

import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import net.minecraft.util.ResourceLocation;

public class TileEntityPrecisionSawmill extends TileEntityChanceMachine
{
	public TileEntityPrecisionSawmill()
	{
		super("PrecisionSawmill.ogg", "PrecisionSawmill", new ResourceLocation("mekanism", "gui/GuiPrecisionSawmill.png"), Mekanism.precisionSawmillUsage, 200, MachineType.PRECISION_SAWMILL.baseEnergy);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.PRECISION_SAWMILL.get();
	}
}
