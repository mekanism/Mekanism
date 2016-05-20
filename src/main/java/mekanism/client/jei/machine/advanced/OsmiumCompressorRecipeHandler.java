package mekanism.client.jei.machine.advanced;

import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.AdvancedMachineRecipeHandler;

public class OsmiumCompressorRecipeHandler extends AdvancedMachineRecipeHandler
{
	public OsmiumCompressorRecipeHandler(AdvancedMachineRecipeCategory c)
	{
		super(c, OsmiumCompressorRecipeWrapper.class);
	}
}
