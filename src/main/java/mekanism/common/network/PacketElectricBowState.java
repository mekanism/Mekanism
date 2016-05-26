package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.network.PacketElectricBowState.ElectricBowStateMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketElectricBowState implements IMessageHandler<ElectricBowStateMessage, IMessage>
{
	@Override
	public IMessage onMessage(ElectricBowStateMessage message, MessageContext context) 
	{
		ItemStack itemstack = PacketHandler.getPlayer(context).getHeldItem(message.currentHand);
		
		if(itemstack != null && itemstack.getItem() instanceof ItemElectricBow)
		{
			((ItemElectricBow)itemstack.getItem()).setFireState(itemstack, message.fireMode);
		}
		
		return null;
	}
	
	public static class ElectricBowStateMessage implements IMessage
	{
		public EnumHand currentHand;
		
		public boolean fireMode;
		
		public ElectricBowStateMessage() {}
	
		public ElectricBowStateMessage(EnumHand hand, boolean state)
		{
			currentHand = hand;
			fireMode = state;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(currentHand.ordinal());
			dataStream.writeBoolean(fireMode);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			currentHand = EnumHand.values()[dataStream.readInt()];
			fireMode = dataStream.readBoolean();
		}
	}
}
