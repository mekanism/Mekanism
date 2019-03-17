package mekanism.common.capabilities;

import mekanism.api.IConfigurable;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by ben on 19/05/16.
 */
public class DefaultConfigurable implements IConfigurable {

    public static void register() {
        CapabilityManager.INSTANCE.register(IConfigurable.class, new NullStorage<>(), DefaultConfigurable::new);
    }

    @Override
    public EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side) {
        return EnumActionResult.PASS;
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side) {
        return EnumActionResult.PASS;
    }
}
