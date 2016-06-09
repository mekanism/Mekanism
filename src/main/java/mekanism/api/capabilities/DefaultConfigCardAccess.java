package mekanism.api.capabilities;

import mekanism.api.IConfigCardAccess;
import mekanism.api.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultConfigCardAccess implements IConfigCardAccess
{
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IConfigCardAccess.class, new NullStorage<IConfigCardAccess>(), DefaultConfigCardAccess.class);
    }
}
