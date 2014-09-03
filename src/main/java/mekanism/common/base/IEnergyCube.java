package mekanism.common.base;

import mekanism.common.Tier.EnergyCubeTier;

import net.minecraft.item.ItemStack;

/**
 * Internal interface used when dealing with Energy Cubes and their tiers.
 * @author AidanBrady
 *
 */
public interface IEnergyCube
{
	/**
	 * Gets the tier of this energy cube.
	 * @param itemstack - ItemStack to check
	 * @return tier
	 */
	public EnergyCubeTier getEnergyCubeTier(ItemStack itemstack);

	/**
	 * Sets the tier of this energy cube
	 * @param itemstack - ItemStack to set
	 * @param tier - tier to set
	 */
	public void setEnergyCubeTier(ItemStack itemstack, EnergyCubeTier tier);
}
