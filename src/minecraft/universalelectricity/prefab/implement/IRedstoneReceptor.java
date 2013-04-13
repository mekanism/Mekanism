package universalelectricity.prefab.implement;

/**
 * This interface should be applied onto all tile entities that needs to receive redstone power.
 * Look at TileEntityBatteryBox for reference.
 * 
 * @author Calclavia
 * 
 */
public interface IRedstoneReceptor
{
	/**
	 * Called when the block is powered on by redstone
	 */
	public void onPowerOn();

	/**
	 * Called when the block is powered off by redstone
	 */
	public void onPowerOff();
}
