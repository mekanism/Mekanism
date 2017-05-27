package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

/**
 * An interface to mark an item as an {@link UpgradableProperty#EnergyStorage} type upgrade
 *
 * @author Player, Chocohead
 */
public interface IEnergyStorageUpgrade extends IUpgradeItem {
	/**
	 * A raw value that is added to the {@link IUpgradableBlock}'s energy storage
	 *
	 * @param stack The upgrade stack
	 * @param parent The block storing energy
	 *
	 * @return The raw change in energy storage to be applied
	 */
	int getExtraEnergyStorage(ItemStack stack, IUpgradableBlock parent);

	/**
	 * A multiplier value that the {@link IUpgradableBlock}'s energy storage is affected by
	 *
	 * @param stack The upgrade stack
	 * @param parent The block storing energy
	 *
	 * @return The multiplier to be applied to be energy storage
	 */
	double getEnergyStorageMultiplier(ItemStack stack, IUpgradableBlock parent);
}