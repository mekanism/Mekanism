package mekanism.common.network.to_server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.robit.RobitSkin;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.SecurityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketRobit implements IMekanismPacket {

    private final RobitPacketType activeType;
    private final int entityId;
    private final ITextComponent name;
    private final RobitSkin skin;

    public PacketRobit(RobitPacketType type, EntityRobit robit) {
        this(type, robit.getId(), null, null);
    }

    public PacketRobit(EntityRobit robit, @Nonnull ITextComponent name) {
        this(RobitPacketType.NAME, robit, name, null);
    }

    public PacketRobit(EntityRobit robit, @Nonnull RobitSkin skin) {
        this(RobitPacketType.SKIN, robit, null, skin);
    }

    private PacketRobit(RobitPacketType type, EntityRobit robit, @Nullable ITextComponent name, @Nullable RobitSkin skin) {
        this(type, robit.getId(), name, skin);
    }

    private PacketRobit(RobitPacketType type, int entityId, @Nullable ITextComponent name, @Nullable RobitSkin skin) {
        activeType = type;
        this.entityId = entityId;
        this.name = name;
        this.skin = skin;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            EntityRobit robit = (EntityRobit) player.level.getEntity(entityId);
            if (robit != null && SecurityUtils.canAccess(player, robit)) {
                if (activeType == RobitPacketType.GO_HOME) {
                    robit.goHome();
                } else if (activeType == RobitPacketType.FOLLOW) {
                    robit.setFollowing(!robit.getFollowing());
                } else if (activeType == RobitPacketType.DROP_PICKUP) {
                    robit.setDropPickup(!robit.getDropPickup());
                } else if (activeType == RobitPacketType.NAME) {
                    robit.setCustomName(name);
                } else if (activeType == RobitPacketType.SKIN) {
                    robit.setSkin(skin, player);
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
        } else if (activeType == RobitPacketType.SKIN) {
            buffer.writeRegistryId(skin);
        }
    }

    public static PacketRobit decode(PacketBuffer buffer) {
        RobitPacketType activeType = buffer.readEnum(RobitPacketType.class);
        int entityId = buffer.readVarInt();
        ITextComponent name = null;
        RobitSkin skin = null;
        if (activeType == RobitPacketType.NAME) {
            name = buffer.readComponent();
        } else if (activeType == RobitPacketType.SKIN) {
            skin = buffer.readRegistryId();
        }
        return new PacketRobit(activeType, entityId, name, skin);
    }

    public enum RobitPacketType {
        GO_HOME,
        FOLLOW,
        DROP_PICKUP,
        NAME,
        SKIN
    }
}