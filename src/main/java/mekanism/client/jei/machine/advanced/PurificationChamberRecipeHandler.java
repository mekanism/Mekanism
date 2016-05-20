package mekanism.client.jei.machine.advanced;

import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.AdvancedMachineRecipeHandler;

public class PurificationChamberRecipeHandler extends AdvancedMachineRecipeHandler
{
	public PurificationChamberRecipeHandler(AdvancedMachineRecipeCategory c)
	{
		super(c, PurificationChamberRecipeWrapper.class);
	}
}
