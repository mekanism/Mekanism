package mekanism.api.transmitters;

import net.minecraftforge.common.ForgeDirection;

public interface IBlockableConnection
{
	public boolean canConnectMutual(ForgeDirection side);
	public boolean canConnect(ForgeDirection side);
}
