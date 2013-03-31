package mekanism.api;

/**
 * Implement this if your block or item can store Mekanism-based upgrades.
 * @author AidanBrady
 *
 */
public interface IUpgradeManagement 
{
	/**
	 * Gets the energy multiplier from an item or block.
	 * @param data - ItemStack parameter if getting from an item
	 * @return energy multiplier
	 */
	public int getEnergyMultiplier(Object... data);
	
	/**
	 * Sets the energy multiplier of an item or block.
	 * @param multiplier - new multiplier
	 * @param data - ItemStack parameter if getting from an item
	 */
	public void setEnergyMultiplier(int multiplier, Object... data);
	
	/**
	 * Gets the speed multiplier from an item or block.
	 * @param data - ItemStack parameter if getting from an item
	 * @return speed multiplier
	 */
	public int getSpeedMultiplier(Object... data);
	
	/**
	 * Sets the speed multiplier of an item or block.
	 * @param multiplier - new multiplier
	 * @param data - ItemStack parameter if getting from an item
	 */
	public void setSpeedMultiplier(int multiplier, Object... data);
	
	/**
	 * Whether or not this item or block supports upgrades.
	 * @param data - ItemStack parameter if getting from an item
	 * @return if the item or block supports upgrades
	 */
	public boolean supportsUpgrades(Object... data);
}
