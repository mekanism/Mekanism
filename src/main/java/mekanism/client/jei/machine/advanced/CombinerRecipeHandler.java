package mekanism.client.jei.machine.advanced;

import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.AdvancedMachineRecipeHandler;

public class CombinerRecipeHandler extends AdvancedMachineRecipeHandler
{
	public CombinerRecipeHandler(AdvancedMachineRecipeCategory c)
	{
		super(c, CombinerRecipeWrapper.class);
	}
}
