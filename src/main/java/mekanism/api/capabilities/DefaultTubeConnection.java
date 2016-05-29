package mekanism.api.capabilities;

import mekanism.api.capabilities.DefaultStorageHelper.NullStorage;
import mekanism.api.gas.ITubeConnection;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultTubeConnection implements ITubeConnection
{
	@Override
	public boolean canTubeConnect(EnumFacing side) 
	{
		return false;
	}
	
	public static void register()
	{
        CapabilityManager.INSTANCE.register(ITubeConnection.class, new NullStorage<>(), DefaultTubeConnection.class);
	}
}
