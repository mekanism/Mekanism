package mekanism.common.capabilities;

import mekanism.api.gas.ITubeConnection;
import mekanism.common.capabilities.StorageHelper.NullStorage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class TubeConnection implements ITubeConnection
{
	@Override
	public boolean canTubeConnect(EnumFacing side) 
	{
		return false;
	}
	
	public static void register()
	{
        CapabilityManager.INSTANCE.register(ITubeConnection.class, new NullStorage<>(), TubeConnection.class);
	}
}
