package mekanism.client;

import mekanism.common.Mekanism;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientPlayerTracker
{
	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event)
	{
		Mekanism.jetpackOn.remove(event.player.getName());
		Mekanism.gasmaskOn.remove(event.player.getName());
		Mekanism.flamethrowerActive.remove(event.player.getName());
		Mekanism.freeRunnerOn.remove(event.player.getName());
	}
}
