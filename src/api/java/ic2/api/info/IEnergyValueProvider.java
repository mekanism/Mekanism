package ic2.api.info;

import net.minecraft.item.ItemStack;

public interface IEnergyValueProvider {
	/**
	 * Determine the energy value for a single item in the supplied stack.
	 * The value is used by most machines in the discharge slot.
	 * 
	 * This only applies to basic single use items, others are to be queried
	 * through e.g. ElectricItem.manager.getCharge().
	 * 
	 * @param itemStack ItemStack containing the item to evaluate.
	 * @return energy in EU
	 */
	double getEnergyValue(ItemStack itemStack);
}
