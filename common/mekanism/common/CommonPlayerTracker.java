package mekanism.common;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketJetpackData.PacketType;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.IPlayerTracker;

public class CommonPlayerTracker implements IPlayerTracker
{
	@Override
	public void onPlayerLogin(EntityPlayer player)
	{
		if(!player.worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketJetpackData().setParams(PacketType.INITIAL), player.worldObj.provider.dimensionId);
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
		Mekanism.jetpackOn.remove(player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
		if(!player.worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketJetpackData().setParams(PacketType.INITIAL), player.worldObj.provider.dimensionId);
		}
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {}
}
