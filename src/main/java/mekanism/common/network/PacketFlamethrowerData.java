package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFlamethrowerData implements IMessageHandler<FlamethrowerDataMessage, IMessage>
{
	@Override
	public IMessage onMessage(FlamethrowerDataMessage message, MessageContext context)
	{
		EntityPlayer player = PacketHandler.getPlayer(context);

        if (message.packetType == FlamethrowerPacket.UPDATE) {
            Mekanism.playerState.setFlamethrowerState(message.username, message.value);

            // If we got this packet on the server, resend out to all clients in same dimension
            // TODO: Why is this a dimensional thing?!
            if (!player.world.isRemote) {
                Mekanism.packetHandler.sendToDimension(message, player.world.provider.getDimension());
            }
        } else if (message.packetType == FlamethrowerPacket.MODE) {
            ItemStack stack = player.getHeldItem(message.currentHand);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemFlamethrower) {
                ((ItemFlamethrower) stack.getItem()).incrementMode(stack);
            }
        }

		return null;
	}
	
	public static class FlamethrowerDataMessage implements IMessage
	{
        public FlamethrowerPacket packetType;

        public EnumHand currentHand;
		public String username;
		public boolean value;
		
		public FlamethrowerDataMessage() {}
	
		public FlamethrowerDataMessage(FlamethrowerPacket type, EnumHand hand, String name, boolean state)
		{
            packetType = type;

            if(type == FlamethrowerPacket.UPDATE)
            {
                username = name;
                value = state;
            }
            else if(type == FlamethrowerPacket.MODE)
            {
            	currentHand = hand;
            }
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
            dataStream.writeInt(packetType.ordinal());

            if(packetType == FlamethrowerPacket.UPDATE)
            {
                PacketHandler.writeString(dataStream, username);
                dataStream.writeBoolean(value);
            }
            else {
            	dataStream.writeInt(currentHand.ordinal());
            }
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
            packetType = FlamethrowerPacket.values()[dataStream.readInt()];

            if(packetType == FlamethrowerPacket.UPDATE)
            {
                username = PacketHandler.readString(dataStream);
                value = dataStream.readBoolean();
            }
            else if(packetType == FlamethrowerPacket.MODE)
            {
            	currentHand = EnumHand.values()[dataStream.readInt()];
            }
		}
	}

    public enum FlamethrowerPacket
    {
        UPDATE,
        MODE
    }
}
