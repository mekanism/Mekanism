package ic2.api.crops;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import ic2.api.info.ILocatable;

/**
 * Interface implemented by the crop tile entity.
 */
public interface ICropTile extends ILocatable {
	/**
	 * Get the crop.
	 *
	 * @return CropCard, or null if there is no plant currently on the crop
	 */
	public CropCard getCrop();

	/**
	 * Set the crop.
	 *
	 * @param cropCard CropCard or null for no plant
	 */
	public void setCrop(CropCard cropCard);

	/**
	 * Get the crop's plant size.
	 *
	 * @return Plant size, starting with 1 and maximum varies depending on plant
	 */
	public int getCurrentSize();

	/**
	 * Set the crop's plant size.
	 *
	 * @param size Plant size
	 */
	public void setCurrentSize(int size);

	/**
	 * Get the crop's plant growth stat.
	 * Higher values indicate faster growth.
	 *
	 * @return Plant growth stat
	 */
	public int getStatGrowth();

	/**
	 * Set the crop's plant growth stat.
	 *
	 * @param growth Plant growth stat
	 */
	public void setStatGrowth(int growth);

	/**
	 * Get the crop's plant gain stat.
	 * Higher values indicate more drops.
	 *
	 * @return Plant gain stat
	 */
	public int getStatGain();

	/**
	 * Set the crop's plant gain stat.
	 *
	 * @param gain Plant gain stat
	 */
	public void setStatGain(int gain);

	/**
	 * Get the crop's plant resistance stat.
	 * Higher values indicate more resistance against trampling.
	 *
	 * @return Plant resistance stat
	 */
	public int getStatResistance();

	/**
	 * Set the crop's plant resistance stat.
	 *
	 * @param resistance Plant resistance stat
	 */
	public void setStatResistance(int resistance);

	/**
	 * Get the crop's nutrient storage.
	 * Ranges from 0 to 100.
	 *
	 * @return Crop nutrient storage
	 */
	public int getStorageNutrients();

	/**
	 * Set the crop's nutrient storage.
	 *
	 * @param nutrients Crop nutrient storage
	 */
	public void setStorageNutrients(int nutrients);

	/**
	 * Get the crop's hydration storage.
	 * 0 indicates nothing, 1-10 indicate water hydration and 11-100 for hydration cells.
	 *
	 * @return Crop hydration storage
	 */
	public int getStorageWater();

	/**
	 * Set the crop's water storage.
	 *
	 * @param water Crop water storage
	 */
	public void setStorageWater(int water);

	/**
	 * Get the crop's Weed-Ex storage.
	 *
	 * @return Crop Weed-Ex storage
	 */
	public int getStorageWeedEX();

	/**
	 * Set the crop's Weed-Ex storage.
	 *
	 * @param weedEX Crop Weed-Ex storage
	 */
	public void setStorageWeedEX(int weedEX);

	/**
	 * Get the crop's plant scan level.
	 * Increases every time the seed is analyzed.
	 *
	 * @return Plant scan level
	 */
	public int getScanLevel();

	/**
	 * Set the crop's plant scan level.
	 *
	 * @param scanLevel Plant scan level
	 */
	public void setScanLevel(int scanLevel);

	public int getGrowthPoints();

	public void setGrowthPoints(int growthPoints);

	public boolean isCrossingBase();

	public void setCrossingBase(boolean crossingBase);

	/**
	 * Get the crop's plant custom data, stored alongside the crop.
	 * Can be modified in place.
	 *
	 * @return Plant custom data
	 */
	public NBTTagCompound getCustomData();

	/**
	 * Get the crop's humidity.
	 * Ranges from 0 (dry) to 10 (humid).
	 * Updates every couple of seconds or when an update is requested.
	 *
	 * @see #updateState()
	 *
	 * @return Crop humidity level
	 */
	public int getTerrainHumidity();

	/**
	 * Get the crop's nutrient level.
	 * Ranges from 0 (empty) to 10 (full).
	 * Updates every couple of seconds or when an update is requested.
	 *
	 * @see #updateState()
	 *
	 * @return Crop nutrient level
	 */
	public int getTerrainNutrients();

	/**
	 * Get the crop's air quality.
	 * Ranges from 0 (cluttered) to 10 (fresh).
	 * Updates every couple of seconds or when an update is requested.
	 *
	 * @see #updateState()
	 *
	 * @return Crop air quality
	 */
	public int getTerrainAirQuality();

	/**
	 * Get the crop's world.
	 * @deprecated This method causes a MethodNotFoundException in a obfuscated environment.
	 * Use {@link #getWorldObj()} instead
	 *
	 * @return Crop world
	 */
	@Deprecated
	public World getWorld();

	/**
	 * Get the crop's location.
	 * @deprecated Use {@link #getPosition()} instead.
	 *
	 * @return Crop location
	 */
	@Deprecated
	public BlockPos getLocation();

	/**
	 * Get the crop's light level.
	 *
	 * @return Crop light level
	 */
	public int getLightLevel();

	/**
	 * Pick the crop, removing and giving seeds for the plant.
	 *
	 * @return true if successfully picked
	 */
	public boolean pick();

	/**
	 * Harvest the crop, turning it into gain and resetting its size.
	 * drop output on ground
	 */
	public boolean performManualHarvest();

	/**
	 * Harvest the crop, turning it into gain and resetting its size.
	 * drop output on ground
	 * @return List<ItemStack> of harvest output
	 */

	public List<ItemStack> performHarvest();

	/**
	 * Fully clears the crop without dropping anything.
	 */
	public void reset();

	/**
	 * Request a texture and lighting update.
	 */
	public void updateState();

	/**
	 * Check if a block is under the farmland containing the crop.
	 * Searches up to 4 blocks below the farmland or an air space, whichever appears first.
	 *
	 * @param block block to search
	 * @return Whether the block was found
	 */
	public boolean isBlockBelow(Block block);

	/**
	 * Check if a block is under the farmland containing the crop.
	 * Searches up to 4 blocks below the farmland or an air space, whichever appears first.
	 *
	 * @param oreDictionaryName blocks to search
	 * @return Whether the blocks were found
	 */
	public boolean isBlockBelow(String oreDictionaryName);

	/**
	 * Generate plant seeds with the given parameters.
	 *
	 * @param crop plant
	 * @param growth plant growth stat
	 * @param gain plant gain stat
	 * @param resistance plant resistance stat
	 * @param scan plant scan level
	 * @return Plant seed item
	 */
	public ItemStack generateSeeds(CropCard crop, int growth, int gain, int resistance, int scan);
}
