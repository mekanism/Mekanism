package mekanism.common.capabilities;

import mekanism.api.transmitters.IBlockableConnection;
import mekanism.common.capabilities.StorageHelper.NullStorage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class BlockableConnection implements IBlockableConnection
{
	@Override
	public boolean canConnectMutual(EnumFacing side) 
	{
		return false;
	}

	@Override
	public boolean canConnect(EnumFacing side) 
	{
		return false;
	}
	
	public static void register()
	{
        CapabilityManager.INSTANCE.register(IBlockableConnection.class, new NullStorage<>(), BlockableConnection.class);
	}
}
