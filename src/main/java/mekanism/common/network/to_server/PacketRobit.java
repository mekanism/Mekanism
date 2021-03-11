package mekanism.common.network.to_server;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketRobit implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            EntityRobit robit = (EntityRobit) player.level.getEntity(entityId);
            if (robit != null) {
                if (activeType == RobitPacketType.FOLLOW) {
                    robit.setFollowing(!robit.getFollowing());
                } else if (activeType == RobitPacketType.NAME) {
                    robit.setCustomName(name);
                } else if (activeType == RobitPacketType.GO_HOME) {
                    robit.goHome();
                } else if (activeType == RobitPacketType.DROP_PICKUP) {
                    robit.setDropPickup(!robit.getDropPickup());
                }
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(activeType);
        buffer.writeVarInt(entityId);
        if (activeType == RobitPacketType.NAME) {
            buffer.writeComponent(name);
        }
    }

    public static PacketRobit decode(PacketBuffer buffer) {
        RobitPacketType activeType = buffer.readEnum(RobitPacketType.class);
        int entityId = buffer.readVarInt();
        if (activeType == RobitPacketType.NAME) {
            return new PacketRobit(entityId, buffer.readComponent());
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