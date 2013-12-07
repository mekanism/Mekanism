package mekanism.client;

import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.IPlayerTracker;

public class ClientPlayerTracker implements IPlayerTracker
{
	@Override
	public void onPlayerLogin(EntityPlayer player) {}

	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
		Mekanism.jetpackOn.remove(player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
		Mekanism.jetpackOn.remove(player);
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {}
}
