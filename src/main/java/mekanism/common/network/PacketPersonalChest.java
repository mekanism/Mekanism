package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketPersonalChest.PersonalChestMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPersonalChest implements IMessageHandler<PersonalChestMessage, IMessage> {

    @Override
    public IMessage onMessage(PersonalChestMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            try {
                Mekanism.proxy.openPersonalChest(player, message.windowId, message.currentHand, message.hotbarSlot);
            } catch (Exception e) {
                Mekanism.logger.error("Error while handling electric chest open packet.", e);
            }
        }, player);
        return null;
    }

    public static class PersonalChestMessage implements IMessage {

        public int windowId;
        public int hotbarSlot;

        public EnumHand currentHand;

        public PersonalChestMessage() {
        }

        public PersonalChestMessage(int window, EnumHand hand, int hotbarSlot) {
            windowId = window;
            currentHand = hand;
            this.hotbarSlot = hotbarSlot;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(windowId);
            dataStream.writeInt(currentHand.ordinal());
            dataStream.writeInt(hotbarSlot);
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            windowId = dataStream.readInt();
            currentHand = EnumHand.values()[dataStream.readInt()];
            hotbarSlot = dataStream.readInt();
        }
    }
}