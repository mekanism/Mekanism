package mekanism.client.jei.machine.basic;

import mekanism.client.jei.machine.MachineRecipeCategory;
import mekanism.client.jei.machine.MachineRecipeHandler;

public class CrusherRecipeHandler extends MachineRecipeHandler
{
	public CrusherRecipeHandler(MachineRecipeCategory c)
	{
		super(c, CrusherRecipeWrapper.class);
	}
}
