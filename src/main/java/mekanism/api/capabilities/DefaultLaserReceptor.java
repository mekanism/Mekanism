package mekanism.api.capabilities;

import mekanism.api.capabilities.DefaultStorageHelper.NullStorage;
import mekanism.api.lasers.ILaserReceptor;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultLaserReceptor implements ILaserReceptor
{
	@Override
	public void receiveLaserEnergy(double energy, EnumFacing side) 
	{
		
	}

	@Override
	public boolean canLasersDig()
	{
		return false;
	}
	
    public static void register()
    {
        CapabilityManager.INSTANCE.register(ILaserReceptor.class, new NullStorage<ILaserReceptor>(), DefaultLaserReceptor.class);
    }
}
