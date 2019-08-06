package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.common.PacketHandler;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.PacketRobit.RobitMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRobit implements IMessageHandler<RobitMessage, IMessage> {

    @Override
    public IMessage onMessage(RobitMessage message, MessageContext context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            EntityRobit robit = (EntityRobit) player.world.getEntityByID(message.entityId);
            if (robit != null) {
                switch (message.activeType) {
                    case GUI:
                        MekanismUtils.openEntityGui(player, robit, message.guiID);
                        break;
                    case FOLLOW:
                        robit.setFollowing(!robit.getFollowing());
                        break;
                    case NAME:
                        robit.setCustomNameTag(message.name);
                        break;
                    case GO_HOME:
                        robit.goHome();
                        break;
                    case DROP_PICKUP:
                        robit.setDropPickup(!robit.getDropPickup());
                        break;
                }
            }
        }, player);
        return null;
    }

    public enum RobitPacketType {
        GUI,
        FOLLOW,
        NAME,
        GO_HOME,
        DROP_PICKUP
    }

    public static class RobitMessage implements IMessage {

        public RobitPacketType activeType;

        public int entityId;
        public int guiID;
        public String name;

        public RobitMessage() {
        }

        public RobitMessage(RobitPacketType type, int entityId) {
            activeType = type;
            this.entityId = entityId;
        }

        public RobitMessage(int entityId, @Nonnull String name) {
            activeType = RobitPacketType.NAME;
            this.entityId = entityId;
            this.name = name;
        }

        public RobitMessage(int entityId, int guiID) {
            activeType = RobitPacketType.GUI;
            this.entityId = entityId;
            this.guiID = guiID;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(activeType.ordinal());
            dataStream.writeInt(entityId);
            if (activeType == RobitPacketType.NAME) {
                PacketHandler.writeString(dataStream, name);
            } else if (activeType == RobitPacketType.GUI) {
                dataStream.writeInt(guiID);
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            activeType = RobitPacketType.values()[dataStream.readInt()];
            entityId = dataStream.readInt();
            if (activeType == RobitPacketType.NAME) {
                name = PacketHandler.readString(dataStream);
            } else if (activeType == RobitPacketType.GUI) {
                guiID = dataStream.readInt();
            }
        }
    }
}