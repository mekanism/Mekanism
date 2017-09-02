package mekanism.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface IMekWrench
{
	boolean canUseWrench(ItemStack stack, EntityPlayer player, BlockPos pos);
}
