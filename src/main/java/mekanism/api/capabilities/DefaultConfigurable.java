package mekanism.api.capabilities;

import mekanism.api.IConfigurable;
import mekanism.api.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by ben on 19/05/16.
 */
public class DefaultConfigurable implements IConfigurable
{
    @Override
    public EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side)
    {
        return EnumActionResult.PASS;
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side)
    {
        return EnumActionResult.PASS;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IConfigurable.class, new NullStorage<IConfigurable>(), DefaultConfigurable.class);
    }
}
