package mekanism.api;

import net.minecraft.src.ItemStack;

/**
 * Implement this in your Item class if it can be used as a machine upgrade.
 * @author AidanBrady
 *
 */
public interface IMachineUpgrade 
{
	/**
	 * The energy boost this upgrade contains.
	 * @param itemstack - stack to check
	 * @return energy boost
	 */
	public int getEnergyBoost(ItemStack itemstack);
	
	/**
	 * The operating tick reduction this upgrade provides.
	 * @param itemstack - stack to check
	 * @return tick reduction
	 */
	public int getTickReduction(ItemStack itemstack);
}
