package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketFlamethrowerActive.FlamethrowerActiveMessage;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketFlamethrowerActive implements IMessageHandler<FlamethrowerActiveMessage, IMessage>
{
	@Override
	public IMessage onMessage(FlamethrowerActiveMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		if(message.value)
		{
			Mekanism.flamethrowerActive.add(player.getCommandSenderName());
		}
		else {
			Mekanism.flamethrowerActive.remove(player.getCommandSenderName());
		}
		
		if(!player.worldObj.isRemote)
		{
			Mekanism.packetHandler.sendToDimension(new FlamethrowerActiveMessage(message.value), player.worldObj.provider.dimensionId);
		}
		
		return null;
	}
	
	public static class FlamethrowerActiveMessage implements IMessage
	{
		public boolean value;
		
		public FlamethrowerActiveMessage() {}
	
		public FlamethrowerActiveMessage(boolean state)
		{
			value = state;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeBoolean(value);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			value = dataStream.readBoolean();
		}
	}
}
