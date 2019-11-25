package mekanism.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface IMekWrench {

    boolean canUseWrench(ItemStack stack, PlayerEntity player, BlockPos pos);
}