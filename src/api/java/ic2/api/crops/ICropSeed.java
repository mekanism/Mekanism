package ic2.api.crops;

import net.minecraft.item.ItemStack;

/**
 * Interface implemented by ItemCropSeed.
 * @author estebes
 */
public interface ICropSeed {
	/**
	 * Get the seed's crop.
	 * @param itemStack The seed's itemstack.
	 * @return Seed's crop.
	 */
	public CropCard getCropFromStack(ItemStack itemStack);

	/**
	 * Set the seed's crop.
	 * @param itemStack The seed's itemstack.
	 * @param crop New CropCard object.
	 */
	public void setCropFromStack(ItemStack itemStack, CropCard crop);

	/**
	 * Get the seed's growth.
	 * @param itemStack The seed's itemstack.
	 * @return Seed's growth.
	 */
	public int getGrowthFromStack(ItemStack itemStack);

	/**
	 * Set the seed's growth.
	 * @param itemStack The seed's itemstack.
	 * @param value New growth value.
	 */
	public void setGrowthFromStack(ItemStack itemStack, int value);

	/**
	 * Get the seed's gain.
	 * @param itemStack The seed's itemstack.
	 * @return Seed's gain.
	 */
	public int getGainFromStack(ItemStack itemStack);

	/**
	 * Set the seed's growth.
	 * @param itemStack The seed's itemstack.
	 * @param value New growth value.
	 */
	public void setGainFromStack(ItemStack itemStack, int value);

	/**
	 * Get the seed's resistance.
	 * @param itemStack The seed's itemstack.
	 * @return Seed's resistance.
	 */
	public int getResistanceFromStack(ItemStack itemStack);

	/**
	 * Set the seed's resistance.
	 * @param itemStack The seed's itemstack.
	 * @param value New resistance value.
	 */
	public void setResistanceFromStack(ItemStack itemStack, int value);

	/**
	 * Get the seed's scan level.
	 * @param itemStack The seed's itemstack.
	 * @return Seed's scan level.
	 */
	public int getScannedFromStack(ItemStack itemStack);

	/**
	 * Set the seed's scan level.
	 * @param itemStack The seed's itemstack.
	 * @param value New scan level value.
	 */
	public void setScannedFromStack(ItemStack itemStack, int value);

	public void incrementScannedFromStack(ItemStack itemStack);
}
