package thermalexpansion.api.item;

import net.minecraft.item.ItemStack;

/**
 * Implement this interface on Item classes that support external manipulation of their internal
 * energy storages. This interface does not provide methods for the underlying internal energy
 * usage.
 */

public interface IChargeableItem
{

	/**
	 * Adds energy to an item. Returns the quantity of energy that was accepted. This should always
	 * return 0 if the item cannot be externally charged.
	 * 
	 * @param theItem ItemStack to be charged.
	 * @param energy Maximum amount of energy to be sent into the item.
	 * @param doReceive If false, the charge will only be simulated.
	 * @return Amount of energy that was accepted by the item.
	 */
	public float receiveEnergy(ItemStack theItem, float energy, boolean doReceive);

	/**
	 * Removes energy from an item. Returns the quantity of energy that was removed. This should
	 * always return 0 if the item cannot be externally discharged.
	 * 
	 * @param theItem ItemStack to be discharged.
	 * @param energy Maximum amount of energy to be removed from the item.
	 * @param doTransfer If false, the discharge will only be simulated.
	 * @return Amount of energy that was removed from the item.
	 */
	public float transferEnergy(ItemStack theItem, float energy, boolean doTransfer);

	/**
	 * Get the amount of energy currently stored in the item.
	 */
	public float getEnergyStored(ItemStack theItem);

	/**
	 * Get the max amount of energy that can be stored in the item.
	 */
	public float getMaxEnergyStored(ItemStack theItem);

}
