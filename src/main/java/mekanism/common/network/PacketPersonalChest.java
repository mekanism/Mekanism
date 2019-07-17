package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketPersonalChest.PersonalChestMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPersonalChest implements IMessageHandler<PersonalChestMessage, IMessage> {

    @Override
    public IMessage onMessage(PersonalChestMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            try {
                Mekanism.proxy.openPersonalChest(player, message.guiType, message.windowId, message.isBlock,
                      message.coord4D == null ? BlockPos.ORIGIN : message.coord4D.getPos(), message.currentHand, message.hotbarSlot);
            } catch (Exception e) {
                Mekanism.logger.error("Error while handling electric chest open packet.", e);
            }
        }, player);
        return null;
    }

    public static class PersonalChestMessage implements IMessage {

        public boolean isBlock;

        public int guiType;
        public int windowId;
        public int hotbarSlot;

        public Coord4D coord4D;

        public EnumHand currentHand;

        public PersonalChestMessage() {
        }

        //This is a really messy implementation...
        public PersonalChestMessage(boolean isBlock, int gui, int window, Coord4D coord, EnumHand hand, int hotbarSlot) {
            guiType = gui;
            windowId = window;
            this.isBlock = isBlock;
            if (this.isBlock) {
                coord4D = coord;
            } else {
                currentHand = hand;
                this.hotbarSlot = hotbarSlot;
            }
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(guiType);
            dataStream.writeInt(windowId);
            dataStream.writeBoolean(isBlock);
            if (isBlock) {
                coord4D.write(dataStream);
            } else {
                dataStream.writeInt(currentHand.ordinal());
                dataStream.writeInt(hotbarSlot);
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            guiType = dataStream.readInt();
            windowId = dataStream.readInt();
            isBlock = dataStream.readBoolean();
            if (isBlock) {
                coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
            } else {
                currentHand = EnumHand.values()[dataStream.readInt()];
                hotbarSlot = dataStream.readInt();
            }
        }
    }
}