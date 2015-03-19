package mekanism.common.base;

import java.util.Map;

import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.MachineOutput;

/**
 * Internal interface containing methods that are shared by many core Mekanism machines.  TODO: remove next minor MC
 * version.
 * @author AidanBrady
 *
 */
public interface IElectricMachine<INPUT extends MachineInput<INPUT>, OUTPUT extends MachineOutput<OUTPUT>, RECIPE extends MachineRecipe<INPUT, OUTPUT, RECIPE>>
{
	/**
	 * Update call for machines. Use instead of updateEntity() - it's called every tick.
	 */
	public void onUpdate();

	/**
	 * Whether or not this machine can operate.
	 * @return can operate
	 */
	public boolean canOperate(RECIPE recipe);

	/**
	 * Runs this machine's operation -- or smelts the item.
	 */
	public void operate(RECIPE recipe);

	/**
	 * Gets this machine's recipes.
	 */
	public Map<INPUT, RECIPE> getRecipes();

	public RECIPE getRecipe();

	public INPUT getInput();
}
