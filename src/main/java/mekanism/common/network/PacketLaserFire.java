package mekanism.common.network;

import mekanism.api.Coord4D;
import mekanism.common.IRedstoneControl.RedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketLaserFire.LaserFireMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketLaserFire implements IMessageHandler<LaserFireMessage, IMessage>
{
	@Override
	public IMessage onMessage(LaserFireMessage message, MessageContext context)
	{
		EntityPlayer player = PacketHandler.getPlayer(context);

		if(Mekanism.proxy.isClientSide())
		{
			Mekanism.proxy.renderLaser(player.worldObj, message.from, message.to, message.direction);
		}

		return null;
	}

	public static class LaserFireMessage implements IMessage
	{
		public Coord4D from;
		public Coord4D to;
		public ForgeDirection direction;

		public LaserFireMessage() {}

		public LaserFireMessage(Coord4D from, Coord4D to, ForgeDirection direction)
		{
			this.from = from;
			this.to = to;
			this.direction = direction;
		}

		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(from.xCoord);
			dataStream.writeInt(from.yCoord);
			dataStream.writeInt(from.zCoord);
			dataStream.writeInt(from.dimensionId);

			dataStream.writeInt(to.xCoord);
			dataStream.writeInt(to.yCoord);
			dataStream.writeInt(to.zCoord);
			dataStream.writeInt(to.dimensionId);

			dataStream.writeInt(direction.ordinal());
		}

		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			from = Coord4D.read(dataStream);
			to = Coord4D.read(dataStream);
			direction = ForgeDirection.getOrientation(dataStream.readInt());
		}
	}
}
