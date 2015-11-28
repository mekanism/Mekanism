package mekanism.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PacketFlamethrowerData implements IMessageHandler<FlamethrowerDataMessage, IMessage>
{
	@Override
	public IMessage onMessage(FlamethrowerDataMessage message, MessageContext context)
	{
		EntityPlayer player = PacketHandler.getPlayer(context);

        if(message.packetType == FlamethrowerPacket.UPDATE)
        {
            if(message.value)
            {
                Mekanism.flamethrowerActive.add(message.username);
            }
            else {
                Mekanism.flamethrowerActive.remove(message.username);
            }

            if(!player.worldObj.isRemote)
            {
                Mekanism.packetHandler.sendToDimension(new FlamethrowerDataMessage(FlamethrowerPacket.UPDATE, message.username, message.value), player.worldObj.provider.dimensionId);
            }
        }
        else if(message.packetType == FlamethrowerPacket.MODE)
        {
            ItemStack stack = player.getCurrentEquippedItem();

            if(stack != null && stack.getItem() instanceof ItemFlamethrower)
            {
                ((ItemFlamethrower)stack.getItem()).incrementMode(stack);
            }
        }
		
		return null;
	}
	
	public static class FlamethrowerDataMessage implements IMessage
	{
        public FlamethrowerPacket packetType;

		public String username;
		public boolean value;
		
		public FlamethrowerDataMessage() {}
	
		public FlamethrowerDataMessage(FlamethrowerPacket type, String name, boolean state)
		{
            packetType = type;

            if(type == FlamethrowerPacket.UPDATE)
            {
                username = name;
                value = state;
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
		}
	}

    public static enum FlamethrowerPacket
    {
        UPDATE,
        MODE;
    }
}
