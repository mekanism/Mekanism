package mekanism.client.jei.machine.chance;

import mekanism.client.jei.machine.ChanceMachineRecipeCategory;
import mekanism.client.jei.machine.ChanceMachineRecipeHandler;

public class PrecisionSawmillRecipeHandler extends ChanceMachineRecipeHandler
{
	public PrecisionSawmillRecipeHandler(ChanceMachineRecipeCategory c)
	{
		super(c, PrecisionSawmillRecipeWrapper.class);
	}
}
