package mekanism.client.sound;

/**
 * Implement this if your TileEntity has a specific sound.
 * @author AidanBrady
 *
 */
public interface IHasSound 
{
	/**
	 * Gets the sound path of this block's sound.
	 * @return sound path
	 */
	public String getSoundPath();
	
	/**
	 * Gets the multiplier to play this sound by.
	 * @return sound multiplier
	 */
	public float getVolumeMultiplier();
}
