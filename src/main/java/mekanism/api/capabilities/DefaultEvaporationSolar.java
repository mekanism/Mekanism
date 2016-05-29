package mekanism.api.capabilities;

import mekanism.api.IEvaporationSolar;
import mekanism.api.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultEvaporationSolar implements IEvaporationSolar
{
	@Override
	public boolean seesSun() 
	{
		return false;
	}
	
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IEvaporationSolar.class, new NullStorage<IEvaporationSolar>(), DefaultEvaporationSolar.class);
    }
}
