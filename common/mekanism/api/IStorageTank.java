package mekanism.api;

import mekanism.api.gas.EnumGas;
import mekanism.api.gas.IGasStorage;
import net.minecraft.item.ItemStack;

/**
 * Implement this in your item class if it can store or transfer certain gasses.
 * @author AidanBrady
 *
 */
public interface IStorageTank extends IGasStorage
{
	/**
	 * Gets the rate of transfer this item can handle.
	 * @return
	 */
	public int getRate();
	
	/**
	 * Adds a defined about of a certain gas to a Storage Tank.
	 * @param itemstack - the itemstack of a Storage Tank to add gas to
	 * @param type - the type of gas to add
	 * @param amount - the amount of gas to add
	 * @return leftover gas
	 */
	public int addGas(ItemStack itemstack, EnumGas type, int amount);
	
	/**
	 * Removes the defined amount of a certain gas from the item.
	 * @param itemstack - the itemstack of a Storage Tank to remove gas from
	 * @param type - the type of gas to remove
	 * @param amount - the amount of gas to remove
	 * @return how much gas was used by this item
	 */
	public int removeGas(ItemStack itemstack, EnumGas type, int amount);
	
	/**
	 * Whether or not this storage tank be given a specific gas.
	 * @param itemstack - the itemstack to check
	 * @param type - the type of gas the tank can possibly receive
	 * @return if the item be charged
	 */
	public boolean canReceiveGas(ItemStack itemstack, EnumGas type);
	
	/**
	 * Whether or not this energized item can give a gas receiver a certain amount of gas.
	 * @param itemstack - the itemstack to check
	 * @param type - the type of gas the tank can possibly provide
	 * @return if the item can provide gas
	 */
	public boolean canProvideGas(ItemStack itemstack, EnumGas type);
	
	/**
	 * Gets this storage tank's current stored gas.
	 * @param itemstack - the itemstack of a Storage Tank to check.
	 * @return which gas the tank is holding
	 */
	public EnumGas getGasType(ItemStack itemstack);
	
	/**
	 * Sets a storage tank's current stored gas.
	 * @param itemstack - the itemstack of a Storage Tank to set.
	 * @param type - the type of gas to change to
	 */
	public void setGasType(ItemStack itemstack, EnumGas type);
}
