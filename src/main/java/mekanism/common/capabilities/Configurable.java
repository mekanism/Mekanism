package mekanism.common.capabilities;

import mekanism.api.IConfigurable;
import mekanism.common.capabilities.StorageHelper.NullStorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by ben on 19/05/16.
 */
public class Configurable implements IConfigurable
{

    @Override
    public boolean onSneakRightClick(EntityPlayer player, EnumFacing side)
    {
        return false;
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumFacing side)
    {
        return false;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IConfigurable.class, new NullStorage<IConfigurable>(), Configurable.class);
    }
}
