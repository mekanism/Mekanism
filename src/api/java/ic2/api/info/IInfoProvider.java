package ic2.api.info;

import net.minecraft.item.ItemStack;

public interface IInfoProvider {
	/**
	 * Determine the energy value for a single item in the supplied stack.
	 * The value is used by most machines in the discharge slot.
	 *
	 * This only applies to basic single use items, others are to be queried
	 * through e.g. ElectricItem.manager.getCharge().
	 *
	 * @param stack ItemStack containing the item to evaluate.
	 * @return energy in EU
	 */
	double getEnergyValue(ItemStack stack);

	/**
	 * Determine the fuel value for a single item in the supplied stack.
	 * The information currently applies to Generators and the Iron Furnace.
	 *
	 * @param stack ItemStack containing the item to evaluate.
	 * @param allowLava Determine if lava has a fuel value, currently only true for the Iron Furnace.
	 * @return fuel value
	 */
	int getFuelValue(ItemStack stack, boolean allowLava);
}
