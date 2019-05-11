package mekanism.common.integration.wrenches;

import mekanism.api.IMekWrench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Translates to IC2's wrench via reflection as it's an internal class.
 */
public class IC2Proxy implements MekWrenchProxy, IMekWrench {

    public static final String IC2_WRENCH_CLASS = "ic2.core.item.tool.ItemToolWrench";

    private final Class<?> wrenchClass;

    public IC2Proxy() {
        Class<?> w = null;
        try {
            w = Class.forName(IC2_WRENCH_CLASS);
        } catch (ClassNotFoundException ignored) {
        }
        wrenchClass = w;
    }

    @Override
    public boolean canUseWrench(ItemStack stack, EntityPlayer player, BlockPos pos) {
        return wrenchClass != null && wrenchClass.isInstance(stack.getItem());
    }

    @Override
    public IMekWrench get(ItemStack stack) {
        return wrenchClass != null && wrenchClass.isInstance(stack.getItem()) ? this : null;
    }
}