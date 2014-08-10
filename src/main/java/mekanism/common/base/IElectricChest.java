package mekanism.common.base;

import net.minecraft.item.ItemStack;

/**
 * Internal interface used for managing Electric Chests.
 * @author aidancbrady
 *
 */
public interface IElectricChest
{
	/**
	 * Sets the 'authenticated' value of this electric chest to a new value.
	 * @param itemStack - electric chest ItemStack
	 * @param auth - new value
	 */
	public void setAuthenticated(ItemStack itemStack, boolean auth);

	/**
	 * Retrieves the 'authenticated' value of this electric chest.
	 * @param itemStack - electric chest ItemStack
	 * @return authenticated value
	 */
	public boolean getAuthenticated(ItemStack itemStack);

	/**
	 * Sets the 'password' value of this electric chest to a new value.
	 * @param itemStack - electric chest ItemStack
	 * @param pass - new value
	 */
	public void setPassword(ItemStack itemStack, String pass);

	/**
	 * Retrieves the 'password' value of this electric chest.
	 * @param itemStack - electric chest ItemStack
	 * @return authenticated value
	 */
	public String getPassword(ItemStack itemStack);

	/**
	 * Sets the 'locked' value of this electric chest to a new value.
	 * @param itemStack - electric chest ItemStack
	 * @param locked - new value
	 */
	public void setLocked(ItemStack itemStack, boolean locked);

	/**
	 * Retrieves the 'locked' value of this electric chest.
	 * @param itemStack - electric chest ItemStack
	 * @return authenticated value
	 */
	public boolean getLocked(ItemStack itemStack);

	/**
	 * Sets the 'open' value of this electric chest to a new value.
	 * @param itemStack - electric chest ItemStack
	 * @param open - new value
	 */
	public void setOpen(ItemStack itemStack, boolean open);

	/**
	 * Retrieves the 'open' value of this electric chest.
	 * @param itemStack - electric chest ItemStack
	 * @return open value
	 */
	public boolean getOpen(ItemStack itemStack);
}
