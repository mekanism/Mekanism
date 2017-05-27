package ic2.api.upgrade;

import java.util.Set;

/**
 * An interface to mark a block as supporting {@link IUpgradeItem}s
 *
 * @author Player
 */
public interface IUpgradableBlock {
	/**
	 * @return The energy the block current has
	 */
	double getEnergy();

	/**
	 * Attempt to use the given amount of energy
	 *
	 * @param amount The amount of energy to be used
	 *
	 * @return Whether the energy was successfully used
	 */
	boolean useEnergy(double amount);

	/**
	 * @return The set of {@link UpgradableProperty}s that the block supports
	 */
	Set<UpgradableProperty> getUpgradableProperties();
}