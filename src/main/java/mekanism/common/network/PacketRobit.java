package mekanism.common.network;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.common.PacketHandler;
import mekanism.common.entity.EntityRobit;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketRobit {

    private RobitPacketType activeType;
    private int entityId;
    private int guiID;
    private String name;

    public PacketRobit(RobitPacketType type, int entityId) {
        activeType = type;
        this.entityId = entityId;
    }

    public PacketRobit(int entityId, @Nonnull String name) {
        activeType = RobitPacketType.NAME;
        this.entityId = entityId;
        this.name = name;
    }

    public PacketRobit(int entityId, int guiID) {
        activeType = RobitPacketType.GUI;
        this.entityId = entityId;
        this.guiID = guiID;
    }

    public static void handle(PacketRobit message, Supplier<Context> context) {
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
    }

    public static void encode(PacketRobit pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.activeType);
        buf.writeInt(pkt.entityId);
        if (pkt.activeType == RobitPacketType.NAME) {
            buf.writeString(pkt.name);
        } else if (pkt.activeType == RobitPacketType.GUI) {
            buf.writeInt(pkt.guiID);
        }
    }

    public static PacketRobit decode(PacketBuffer buf) {
        RobitPacketType activeType = buf.readEnumValue(RobitPacketType.class);
        int entityId = buf.readInt();
        if (activeType == RobitPacketType.NAME) {
            return new PacketRobit(entityId, buf.readString());
        } else if (activeType == RobitPacketType.GUI) {
            return new PacketRobit(entityId, buf.readInt());
        }
        return new PacketRobit(activeType, entityId);
    }

    public enum RobitPacketType {
        GUI,
        FOLLOW,
        NAME,
        GO_HOME,
        DROP_PICKUP
    }
}