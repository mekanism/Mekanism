package mekanism.api;

import net.minecraftforge.common.ForgeDirection;

public interface ITubeConnection 
{
	/**
	 * Whether or not a tube can connect to a certain orientation.
	 * @param side - orientation to check
	 * @return if a tube can connect
	 */
	public boolean canTubeConnect(ForgeDirection side);
}
