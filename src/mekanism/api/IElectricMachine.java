package mekanism.api;

import java.util.Map;

/**
 * A group of common methods used by all Mekanism machines.
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
	 * Gets the recipe vector from the machine tile entity.
	 * @return recipes
	 */
	public Map getRecipes();
}
