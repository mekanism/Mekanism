package mekanism.common.network;

import mekanism.common.PacketHandler;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.network.PacketElectricBowState.ElectricBowStateMessage;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketElectricBowState implements IMessageHandler<ElectricBowStateMessage, IMessage>
{
	@Override
	public IMessage onMessage(ElectricBowStateMessage message, MessageContext context) 
	{
		ItemStack itemstack = PacketHandler.getPlayer(context).getCurrentEquippedItem();
		
		if(itemstack != null && itemstack.getItem() instanceof ItemElectricBow)
		{
			((ItemElectricBow)itemstack.getItem()).setFireState(itemstack, message.fireMode);
		}
		
		return null;
	}
	
	public static class ElectricBowStateMessage implements IMessage
	{
		public boolean fireMode;
		
		public ElectricBowStateMessage() {}
	
		public ElectricBowStateMessage(boolean state)
		{
			fireMode = state;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeBoolean(fireMode);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			fireMode = dataStream.readBoolean();
		}
	}
}
