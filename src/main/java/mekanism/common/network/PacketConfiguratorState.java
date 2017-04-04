package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.network.PacketConfiguratorState.ConfiguratorStateMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketConfiguratorState implements IMessageHandler<ConfiguratorStateMessage, IMessage>
{
	@Override
	public IMessage onMessage(ConfiguratorStateMessage message, MessageContext context) 
	{
		ItemStack itemstack = PacketHandler.getPlayer(context).getHeldItem(message.currentHand);
		
		if(!itemstack.isEmpty() && itemstack.getItem() instanceof ItemConfigurator)
		{
			((ItemConfigurator)itemstack.getItem()).setState(itemstack, message.state);
		}
		
		return null;
	}
	
	public static class ConfiguratorStateMessage implements IMessage
	{
		public EnumHand currentHand;
		
		public ConfiguratorMode state;
		
		public ConfiguratorStateMessage() {}
	
		public ConfiguratorStateMessage(EnumHand hand, ConfiguratorMode s)
		{
			currentHand = hand;
			state = s;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(currentHand.ordinal());
			dataStream.writeInt(state.ordinal());
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			currentHand = EnumHand.values()[dataStream.readInt()];
			state = ConfiguratorMode.values()[dataStream.readInt()];
		}
	}
}
