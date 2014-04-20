package mekanism.common;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketBoxBlacklist;
import mekanism.common.network.PacketConfigSync;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketJetpackData.JetpackPacket;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketScubaTankData.ScubaTankPacket;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class CommonPlayerTracker
{
	@SubscribeEvent
	public void onPlayerLoginEvent(PlayerLoggedInEvent event)
	{
		onPlayerLogin(event.player);
	}

	@Override
	public void onPlayerLogin(EntityPlayer player)
	{
		if(!player.getWorldObj().isRemote)
		{
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketConfigSync().setParams(), player);
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketBoxBlacklist().setParams(), player);
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketJetpackData().setParams(JetpackPacket.FULL), player);
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketScubaTankData().setParams(ScubaTankPacket.FULL), player);

			System.out.println("[Mekanism] Sent config to '" + player.username + ".'");
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
		Mekanism.jetpackOn.remove(player);
		Mekanism.gasmaskOn.remove(player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
		Mekanism.jetpackOn.remove(player);

		if(!player.getWorldObj().isRemote)
		{
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketJetpackData().setParams(JetpackPacket.FULL), player);
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketScubaTankData().setParams(ScubaTankPacket.FULL), player);
		}
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {}
}
