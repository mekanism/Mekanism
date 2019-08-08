package mekanism.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

public interface IMekWrench {

    boolean canUseWrench(ItemStack stack, PlayerEntity player, BlockPos pos);

    /* easier compat with BC */
    default boolean canUseWrench(PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult rayTrace) {
        return canUseWrench(stack, player, rayTrace.getPos());
    }

    default void wrenchUsed(PlayerEntity player, Hand hand, ItemStack wrench, BlockRayTraceResult rayTrace) {
    }
}