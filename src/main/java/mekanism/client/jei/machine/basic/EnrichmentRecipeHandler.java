package mekanism.client.jei.machine.basic;

import mekanism.client.jei.machine.MachineRecipeCategory;
import mekanism.client.jei.machine.MachineRecipeHandler;

public class EnrichmentRecipeHandler extends MachineRecipeHandler
{
	public EnrichmentRecipeHandler(MachineRecipeCategory c)
	{
		super(c, EnrichmentRecipeWrapper.class);
	}
}
