package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import mekanism.common.PacketHandler;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.PacketRobit.RobitMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRobit implements IMessageHandler<RobitMessage, IMessage> {

    @Override
    public IMessage onMessage(RobitMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            EntityRobit robit = (EntityRobit) player.world.getEntityByID(message.entityId);
            if (robit != null) {
                switch (message.activeType) {
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
        FOLLOW,
        NAME,
        GO_HOME,
        DROP_PICKUP
    }

    public static class RobitMessage implements IMessage {

        public RobitPacketType activeType;

        public int entityId;

        public String name;

        public RobitMessage() {
        }

        public RobitMessage(RobitPacketType type, int entityId, @Nullable String name) {
            activeType = type;
            this.entityId = entityId;
            if (activeType == RobitPacketType.NAME) {
                this.name = name;
            }
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(activeType.ordinal());
            switch (activeType) {
                case FOLLOW:
                    dataStream.writeInt(entityId);
                    break;
                case NAME:
                    PacketHandler.writeString(dataStream, name);
                    dataStream.writeInt(entityId);
                    break;
                case GO_HOME:
                    dataStream.writeInt(entityId);
                    break;
                case DROP_PICKUP:
                    dataStream.writeInt(entityId);
                    break;
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            activeType = RobitPacketType.values()[dataStream.readInt()];
            if (activeType == RobitPacketType.FOLLOW) {
                entityId = dataStream.readInt();
            } else if (activeType == RobitPacketType.NAME) {
                name = PacketHandler.readString(dataStream);
                entityId = dataStream.readInt();
            } else if (activeType == RobitPacketType.GO_HOME) {
                entityId = dataStream.readInt();
            } else if (activeType == RobitPacketType.DROP_PICKUP) {
                entityId = dataStream.readInt();
            }
        }
    }
}