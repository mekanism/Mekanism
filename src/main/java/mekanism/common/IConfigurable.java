package mekanism.common;

import net.minecraft.entity.player.EntityPlayer;

public interface IConfigurable
{
	public boolean onSneakRightClick(EntityPlayer player, int side);

	public boolean onRightClick(EntityPlayer player, int side);
}
