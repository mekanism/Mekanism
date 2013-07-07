package universalelectricity.core.item;

import net.minecraft.item.ItemStack;

/**
 * Applies to items that has a voltage.
 * 
 * @author Calclavia
 * 
 */
public interface IItemVoltage
{

	/**
	 * @return The voltage in which this item runs on.
	 */
	public double getVoltage(ItemStack itemStack);
}
