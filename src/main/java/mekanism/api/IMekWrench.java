package mekanism.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public interface IMekWrench
{
	public boolean canUseWrench(EntityPlayer player, BlockPos pos);
}
