package mekanism.common.network;

import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.network.PacketRedstoneControl.RedstoneControlMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketRedstoneControl implements IMessageHandler<RedstoneControlMessage, IMessage>
{
	@Override
	public IMessage onMessage(RedstoneControlMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		TileEntity tileEntity = message.coord4D.getTileEntity(player.worldObj);
		
		if(tileEntity instanceof IRedstoneControl)
		{
			((IRedstoneControl)tileEntity).setControlType(message.value);
		}
		
		return null;
	}
	
	public static class RedstoneControlMessage implements IMessage
	{
		public Coord4D coord4D;
		public RedstoneControl value;
		
		public RedstoneControlMessage() {}
	
		public RedstoneControlMessage(Coord4D coord, RedstoneControl control)
		{
			coord4D = coord;
			value = control;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(coord4D.xCoord);
			dataStream.writeInt(coord4D.yCoord);
			dataStream.writeInt(coord4D.zCoord);
			dataStream.writeInt(coord4D.dimensionId);
	
			dataStream.writeInt(value.ordinal());
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			coord4D = Coord4D.read(dataStream);
			value = RedstoneControl.values()[dataStream.readInt()];
		}
	}
}
