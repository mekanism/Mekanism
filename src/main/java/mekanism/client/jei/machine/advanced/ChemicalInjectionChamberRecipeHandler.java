package mekanism.client.jei.machine.advanced;

import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.AdvancedMachineRecipeHandler;

public class ChemicalInjectionChamberRecipeHandler extends AdvancedMachineRecipeHandler
{
	public ChemicalInjectionChamberRecipeHandler(AdvancedMachineRecipeCategory c)
	{
		super(c, ChemicalInjectionChamberRecipeWrapper.class);
	}
}
