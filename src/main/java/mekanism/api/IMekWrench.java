package mekanism.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public interface IMekWrench {

    boolean canUseWrench(ItemStack stack, EntityPlayer player, BlockPos pos);

    /* easier compat with BC */
    default boolean canUseWrench(EntityPlayer player, EnumHand hand, ItemStack stack, RayTraceResult rayTrace) {
        return canUseWrench(stack, player, rayTrace.getBlockPos());
    }

    default void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
    }
}
