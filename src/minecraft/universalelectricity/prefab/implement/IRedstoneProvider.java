package universalelectricity.prefab.implement;

import net.minecraftforge.common.ForgeDirection;

/**
 * This should be applied on tile entities that can provide redstone power
 * 
 * @author Calclavia
 * 
 */
public interface IRedstoneProvider
{
	public boolean isPoweringTo(ForgeDirection side);

	public boolean isIndirectlyPoweringTo(ForgeDirection side);
}
