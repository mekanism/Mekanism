package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

/**
 * An interface to mark an item as an {@link UpgradableProperty#Processing} type upgrade
 *
 * @author Player, Chocohead
 */
public interface IProcessingUpgrade extends IUpgradeItem {
	/**
	 * A raw value that the process time is affected by (in ticks)
	 *
	 * @param stack The upgrade stack
	 * @param parent The {@link IUpgradableBlock} receiving the change
	 *
	 * @return The raw change in process to be applied
	 */
	int getExtraProcessTime(ItemStack stack, IUpgradableBlock parent);

	/**
	 * A multiplier value that the process time is affected by
	 *
	 * @param stack The upgrade stack
	 * @param parent The {@link IUpgradableBlock} receiving the change
	 *
	 * @return The multiplier to be applied to be progress
	 */
	double getProcessTimeMultiplier(ItemStack stack, IUpgradableBlock parent);

	/**
	 * A raw value that is added to the {@link IUpgradableBlock}'s energy demand
	 *
	 * @param stack The upgrade stack
	 * @param parent The block consuming energy
	 *
	 * @return The raw change in energy demand to be applied
	 */
	int getExtraEnergyDemand(ItemStack stack, IUpgradableBlock parent);

	/**
	 * A multiplier value that the {@link IUpgradableBlock}'s energy demand is affected by
	 *
	 * @param stack The upgrade stack
	 * @param parent The block consuming energy
	 *
	 * @return The multiplier to be applied to be energy demand
	 */
	double getEnergyDemandMultiplier(ItemStack stack, IUpgradableBlock parent);
}