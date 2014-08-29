package mekanism.common.tile;

import java.util.Map;

import mekanism.api.MekanismConfig.usage;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class TileEntityPrecisionSawmill extends TileEntityChanceMachine
{
	public TileEntityPrecisionSawmill()
	{
		super("sawmill", "PrecisionSawmill", MekanismUtils.getResource(ResourceType.GUI, "GuiBasicMachine.png"), usage.precisionSawmillUsage, 200, MachineType.PRECISION_SAWMILL.baseEnergy);
	}

	@Override
	public Map getRecipes()
	{
		return Recipe.PRECISION_SAWMILL.get();
	}

	@Override
	public float getVolume()
	{
		return 0.7F;
	}
}
