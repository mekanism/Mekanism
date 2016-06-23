package mekanism.common.capabilities;

import mekanism.api.energy.ICableOutputter;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by ben on 30/04/16.
 */
public class DefaultCableOutputter implements ICableOutputter
{
    @Override
    public boolean canOutputTo(EnumFacing side)
    {
        return true;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(ICableOutputter.class, new NullStorage<>(), DefaultCableOutputter.class);
    }
}
