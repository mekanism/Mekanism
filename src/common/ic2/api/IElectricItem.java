package ic2.api;

/**
 * Provides the ability to store energy on the implementing item.
 * 
 * The item should have a maximum damage of 13.
 */
public interface IElectricItem {
	/**
	 * Determine if the item can be used in a machine to supply energy.
	 * 
	 * @return Whether the item can supply energy
	 */
	boolean canProvideEnergy();
	
	/**
	 * Get the item ID to use for a charge energy greater than 0.
	 * 
	 * @return Item ID to use
	 */
	int getChargedItemId();
	
	/**
	 * Get the item ID to use for a charge energy of 0.
	 * 
	 * @return Item ID to use
	 */
	int getEmptyItemId();
	
	/**
	 * Get the item's maximum charge energy in EU.
	 * 
	 * @return Maximum charge energy
	 */
	int getMaxCharge();
	
	/**
	 * Get the item's tier, lower tiers can't send energy to higher ones.
	 * Batteries are Tier 1, Energy Crystals are Tier 2, Lapotron Crystals are Tier 3.
	 * 
	 * @return Item's tier
	 */
	int getTier();
	
	/**
	 * Get the item's transfer limit in EU per transfer operation.
	 * 
	 * @return Transfer limit
	 */
	int getTransferLimit();
}

