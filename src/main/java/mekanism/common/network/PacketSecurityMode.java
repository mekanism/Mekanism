package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketSecurityMode.SecurityModeMessage;
import mekanism.common.security.ISecurity;
import mekanism.common.security.ISecurity.SecurityMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSecurityMode implements IMessageHandler<SecurityModeMessage, IMessage> 
{
	@Override
	public IMessage onMessage(SecurityModeMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		TileEntity tileEntity = message.coord4D.getTileEntity(player.worldObj);
		
		if(tileEntity instanceof ISecurity)
		{
			String owner = ((ISecurity)tileEntity).getSecurity().getOwner();
			
			if(owner != null && player.getCommandSenderName().equals(owner))
			{
				((ISecurity)tileEntity).getSecurity().setMode(message.value);
			}
		}
		
		return null;
	}
	
	public static class SecurityModeMessage implements IMessage
	{
		public Coord4D coord4D;
		public SecurityMode value;
		
		public SecurityModeMessage() {}
	
		public SecurityModeMessage(Coord4D coord, SecurityMode control)
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
			value = SecurityMode.values()[dataStream.readInt()];
		}
	}
}
