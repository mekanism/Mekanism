package mekanism.common.network.to_server.robit;

import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.Util;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketRobitName(int entityId, String name) implements IMekanismPacket {

    public static final int MAX_NAME_LENGTH = 50;
    public static final CustomPacketPayload.Type<PacketRobitName> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("robit_name"));
    public static final StreamCodec<ByteBuf, PacketRobitName> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, PacketRobitName::entityId,
          ByteBufCodecs.stringUtf8(MAX_NAME_LENGTH), PacketRobitName::name,
          PacketRobitName::new
    );

    private static final Map<String, List<ResourceKey<RobitSkin>>> EASTER_EGGS = Map.of(
          "sara", getPrideSkins(RobitPrideSkinData.TRANS, RobitPrideSkinData.LESBIAN)
    );

    private static List<ResourceKey<RobitSkin>> getPrideSkins(RobitPrideSkinData... prideSkinData) {
        return Stream.of(prideSkinData).map(MekanismRobitSkins.PRIDE_SKINS::get).toList();
    }

    public PacketRobitName(EntityRobit robit, String name) {
        this(robit.getId(), name);
    }

    public PacketRobitName {
        //This should already be trimmed, but we want to validate that is the case
        name = name.trim();
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketRobitName> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (!hasContent(name)) {//Ensure the client sent a valid string
            return;
        }
        Player player = context.player();
        Entity entity = player.level().getEntity(entityId);
        //Validate the player can access the robit before changing the robit's name
        if (entity instanceof EntityRobit robit && IEntitySecurityUtils.INSTANCE.canAccess(player, robit)) {
            robit.setCustomName(TextComponentUtil.getString(name));
            if (robit.getSkin() == MekanismRobitSkins.BASE) {
                //If the robit has the base skin currently equipped, check if there are any skins paired with the name that got set as an Easter egg,
                // and then pick a random one and set it
                // Note: We use null for the player instead of the actual player in case we ever
                // end up adding any Easter egg skins that aren't unlocked by default, to still
                // be able to equip them. We already validate the player can access the robit
                // above before setting the name
                Optional<ResourceKey<RobitSkin>> randomSkin = Util.getRandomSafe(EASTER_EGGS.getOrDefault(name.toLowerCase(Locale.ROOT), Collections.emptyList()), robit.level().random);
                //noinspection OptionalIsPresent - Capturing lambda
                if (randomSkin.isPresent()) {
                    robit.setSkin(randomSkin.get(), null);
                }
            }
        }
    }

    public static boolean hasContent(String text) {
        //TODO: Maybe improve on how we do color codes to allow further customization beyond the basic color codes?
        if (!text.isEmpty()) {
            boolean wasColorSymbol = false;
            for (char c : text.toCharArray()) {
                if (c == 167) {
                    wasColorSymbol = true;
                } else if (!wasColorSymbol) {
                    return true;
                } else {
                    wasColorSymbol = false;
                }
            }
        }
        return false;
    }
}