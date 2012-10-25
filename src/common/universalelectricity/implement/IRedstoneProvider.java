package universalelectricity.implement;

/**
 * This should be applied on tile entities that can provide redstone power
 * 
 * @author Henry
 * 
 */
public interface IRedstoneProvider
{
	public boolean isPoweringTo(byte side);

	public boolean isIndirectlyPoweringTo(byte side);
}
