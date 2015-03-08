package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.network.PacketConfiguratorState.ConfiguratorStateMessage;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketConfiguratorState implements IMessageHandler<ConfiguratorStateMessage, IMessage>
{
	@Override
	public IMessage onMessage(ConfiguratorStateMessage message, MessageContext context) 
	{
		ItemStack itemstack = PacketHandler.getPlayer(context).getCurrentEquippedItem();
		
		if(itemstack != null && itemstack.getItem() instanceof ItemConfigurator)
		{
			((ItemConfigurator)itemstack.getItem()).setState(itemstack, message.state);
		}
		
		return null;
	}
	
	public static class ConfiguratorStateMessage implements IMessage
	{
		public ConfiguratorMode state;
		
		public ConfiguratorStateMessage() {}
	
		public ConfiguratorStateMessage(ConfiguratorMode s)
		{
			state = s;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(state.ordinal());
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			state = ConfiguratorMode.values()[dataStream.readInt()];
		}
	}
}
