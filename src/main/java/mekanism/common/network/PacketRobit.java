package mekanism.common.network;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketRobit {

    private final RobitPacketType activeType;
    private final int entityId;
    private ITextComponent name;

    public PacketRobit(RobitPacketType type, int entityId) {
        activeType = type;
        this.entityId = entityId;
    }

    public PacketRobit(int entityId, @Nonnull ITextComponent name) {
        activeType = RobitPacketType.NAME;
        this.entityId = entityId;
        this.name = name;
    }

    public static void handle(PacketRobit message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            if (player != null) {
                EntityRobit robit = (EntityRobit) player.world.getEntityByID(message.entityId);
                if (robit != null) {
                    if (message.activeType == RobitPacketType.FOLLOW) {
                        robit.setFollowing(!robit.getFollowing());
                    } else if (message.activeType == RobitPacketType.NAME) {
                        robit.setCustomName(message.name);
                    } else if (message.activeType == RobitPacketType.GO_HOME) {
                        robit.goHome();
                    } else if (message.activeType == RobitPacketType.DROP_PICKUP) {
                        robit.setDropPickup(!robit.getDropPickup());
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketRobit pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.activeType);
        buf.writeVarInt(pkt.entityId);
        if (pkt.activeType == RobitPacketType.NAME) {
            buf.writeTextComponent(pkt.name);
        }
    }

    public static PacketRobit decode(PacketBuffer buf) {
        RobitPacketType activeType = buf.readEnumValue(RobitPacketType.class);
        int entityId = buf.readVarInt();
        if (activeType == RobitPacketType.NAME) {
            return new PacketRobit(entityId, buf.readTextComponent());
        }
        return new PacketRobit(activeType, entityId);
    }

    public enum RobitPacketType {
        FOLLOW,
        NAME,
        GO_HOME,
        DROP_PICKUP
    }
}