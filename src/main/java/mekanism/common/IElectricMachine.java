package mekanism.common;

import java.util.Map;

/**
 * Internal interface containing methods that are shared by many core Mekanism machines.  TODO: remove next minor MC
 * version.
 * @author AidanBrady
 *
 */
public interface IElectricMachine
{
	/**
	 * Update call for machines. Use instead of updateEntity() - it's called every tick.
	 */
	public void onUpdate();

	/**
	 * Whether or not this machine can operate.
	 * @return can operate
	 */
	public boolean canOperate();

	/**
	 * Runs this machine's operation -- or smelts the item.
	 */
	public void operate();

	/**
	 * Gets this machine's recipes.
	 */
	public Map getRecipes();
}
