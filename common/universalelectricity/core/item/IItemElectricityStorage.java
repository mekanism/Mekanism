package universalelectricity.core.item;

import net.minecraft.item.ItemStack;

public interface IItemElectricityStorage
{
	/**
	 * Returns the amount of joules this unit has stored.
	 */
	public double getJoules(ItemStack itemStack);

	/**
	 * Sets the amount of joules this unit has stored.
	 */
	public void setJoules(double joules, ItemStack itemStack);

	/**
	 * Gets the maximum amount of joules this unit can store.
	 */
	public double getMaxJoules(ItemStack itemStack);
}
