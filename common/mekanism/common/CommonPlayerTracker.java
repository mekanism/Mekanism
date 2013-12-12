package mekanism.common;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketJetpackData.JetpackPacket;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketScubaTankData.ScubaTankPacket;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.IPlayerTracker;

public class CommonPlayerTracker implements IPlayerTracker
{
	@Override
	public void onPlayerLogin(EntityPlayer player) 
	{
		if(!player.worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketJetpackData().setParams(JetpackPacket.FULL), player.worldObj.provider.dimensionId);
			PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketScubaTankData().setParams(ScubaTankPacket.FULL), player.worldObj.provider.dimensionId);
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
		Mekanism.jetpackOn.remove(player);
		
		if(!player.worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketJetpackData().setParams(JetpackPacket.FULL), player.worldObj.provider.dimensionId);
			PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketScubaTankData().setParams(ScubaTankPacket.FULL), player.worldObj.provider.dimensionId);
		}
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
		Mekanism.jetpackOn.remove(player);
		
		if(!player.worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketJetpackData().setParams(JetpackPacket.FULL), player.worldObj.provider.dimensionId);
			PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketScubaTankData().setParams(ScubaTankPacket.FULL), player.worldObj.provider.dimensionId);
		}
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {}
}
