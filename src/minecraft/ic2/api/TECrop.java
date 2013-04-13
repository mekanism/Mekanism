package ic2.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * Provides access to a crop tile entity. Contains all methods and fields you can access from your CropCard.
 */
public abstract class TECrop extends TileEntity
{
	/**
	 * ID of the plant currently on the crop.
	 * -1 if there is no plant currently on the crop.
	 */
	public short id = -1;
	
	/**
	 * Current size of the plant.
	 * 1 is the starting size and the maximum size varies from plant to plant.
	 */
	public byte size = 0;
	
	/**
	 * Growth ability of a plant.
	 * Higher values give faster growth.
	 */
	public byte statGrowth = 0;
	/**
	 * Chances and amount of gain and seeds from harvested crops.
	 * Higher values give more drops.
	 */
	public byte statGain = 0;
	/**
	 * Ability for the plant to resist trampling.
	 * Higher values give more resistance against trampling.
	 */
	public byte statResistance = 0;
	
	/**
	 * Scan level, increases each time a seed goes through analysis.
	 */
	public byte scanLevel = 0;
	
	/**
	 * Custom data stored along a TECrop.
	 */
	public short[] custumData = new short[16];
	
	/**
	 * Crop nutrients.
	 * Ranges from 0 (empty) to 100 (full).
	 */
	public int nutrientStorage = 0;
	/**
	 * Crop hydration.
	 * Values are:
	 * - 0 for nothing
	 * - 1-10 for water hydration
	 * - 11-100 for distilled water (hydration cell) hydration
	 */
	public int waterStorage = 0;
	/**
	 * Remaining duration of WeedEX
	 * PRevents weed from growing and protects against (unimplemented) insects or similar stuff
	 */
	public int exStorage = 0;
	
	/**
	 * Crop humidity.
	 * Ranges from 0 (dry) to 10 (humid).
	 * Updates every couple of seconds or when an update is requested.
	 * 
	 * @see #updateState()
	 */
	public abstract byte getHumidity();
	
	/**
	 * Get the crop's nutrient level.
	 * Ranges from 0 (empty) to 10 (full).
	 * Updates every couple of seconds or when an update is requested.
	 * 
	 * @see #updateState()
	 */
	public abstract byte getNutrients();
	
	/**
	 * Get the crop's air quality. 
	 * Ranges from 0 (cluttered) to 10 (fresh).
	 * Updates every couple of seconds or when an update is requested.
	 * 
	 * @see #updateState()
	 * 
	 * @return Crop air quality
	 */
	public abstract byte getAirQuality();
	
	/**
	 * Get the crop's light level.
	 * 
	 * @return Crop light level
	 */
	public int getLightLevel()
	{
		return worldObj.getBlockLightValue(xCoord, yCoord, zCoord);
	}
	
	
	/**
	 * Pick the crop, removing and giving seeds for the plant. 
	 * 
	 * @param manual whether it was done by hand (not automated)
	 * @return true if successfully picked
	 */
	public abstract boolean pick(boolean manual);
	
	/**
	 * Harvest the crop, turning it into gain and resetting its size.
	 * 
	 * @param manual whether it one by hand (not automated)
	 * @return true if successfully harvested
	 */
	public abstract boolean harvest(boolean manual);
	
	/**
	 * Fully clears the crop without dropping anything.
	 */
	public abstract void reset();

	/**
	 * Request a texture and lighting update.
	 */
	public abstract void updateState();

	/**
	 * Check if a block is under the farmland containing the crop.
	 * Searches up to 2 blocks below the farmland or an air space, whichever appears first.
	 * 
	 * @param block block to search
	 * @return Whether the block was found
	 */
	public abstract boolean isBlockBelow(Block block);

	/**
	 * Generate plant seeds with the given parameters.
	 * 
	 * @param plant plant ID
	 * @param growth plant growth stat
	 * @param gain plant gain stat
	 * @param resis plant resistance stat
	 * @param scan plant scan level
	 * @return Plant seed item
	 */
	public abstract ItemStack generateSeeds(short plant, byte growth, byte gain, byte resis, byte scan);
	
	/**
	 * For internal usage only.
	 */
	public abstract void addLocal(String s1, String s2);
	
}
