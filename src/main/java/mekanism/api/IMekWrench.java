package mekanism.api;

import net.minecraft.entity.player.EntityPlayer;

public interface IMekWrench
{
	public boolean canUseWrench(EntityPlayer player, int x, int y, int z);
}
