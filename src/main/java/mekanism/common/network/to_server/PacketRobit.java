package mekanism.common.network.to_server;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.providers.IRobitSkinProvider;
import mekanism.api.robit.RobitSkin;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.util.SecurityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketRobit implements IMekanismPacket {

    private static final Map<String, List<IRobitSkinProvider>> EASTER_EGGS = ImmutableMap.<String, List<IRobitSkinProvider>>builder()
          .put("sara", Arrays.asList(MekanismRobitSkins.LESBIAN, MekanismRobitSkins.TRANS))//TODO - 1.18: Make this use Lists.of(...)
          .build();

    private final RobitPacketType activeType;
    private final int entityId;
    private final String name;
    private final RobitSkin skin;

    public PacketRobit(RobitPacketType type, EntityRobit robit) {
        this(type, robit.getId(), null, null);
    }

    public PacketRobit(EntityRobit robit, @Nonnull String name) {
        this(RobitPacketType.NAME, robit, name, null);
    }

    public PacketRobit(EntityRobit robit, @Nonnull RobitSkin skin) {
        this(RobitPacketType.SKIN, robit, null, skin);
    }

    private PacketRobit(RobitPacketType type, EntityRobit robit, @Nullable String name, @Nullable RobitSkin skin) {
        this(type, robit.getId(), name, skin);
    }

    private PacketRobit(RobitPacketType type, int entityId, @Nullable String name, @Nullable RobitSkin skin) {
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
                    robit.setCustomName(TextComponentUtil.getString(name));
                    if (robit.getSkin() == MekanismRobitSkins.BASE.get()) {
                        //If the robit has the base skin currently equipped
                        List<IRobitSkinProvider> skins = EASTER_EGGS.getOrDefault(name.toLowerCase(Locale.ROOT), Collections.emptyList());
                        // check if there are any skins paired with the name that got set as an Easter egg
                        if (!skins.isEmpty()) {
                            // if there are, then pick a random one and set it
                            // Note: We use null for the player instead of the actual player in case we ever
                            // end up adding any Easter egg skins that aren't unlocked by default, to still
                            // be able to equip them. We already validate the player can access the robit
                            // above before setting the name
                            robit.setSkin(skins.get(robit.level.random.nextInt(skins.size())), null);
                        }
                    }
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
            buffer.writeUtf(name);
        } else if (activeType == RobitPacketType.SKIN) {
            buffer.writeRegistryId(skin);
        }
    }

    public static PacketRobit decode(PacketBuffer buffer) {
        RobitPacketType activeType = buffer.readEnum(RobitPacketType.class);
        int entityId = buffer.readVarInt();
        String name = null;
        RobitSkin skin = null;
        if (activeType == RobitPacketType.NAME) {
            name = BasePacketHandler.readString(buffer).trim();
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