package mekanism.common.network.to_client.security;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PacketSyncSecurity(UUID playerUUID, String playerUsername, @Nullable SecurityData securityData) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketSyncSecurity> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("sync_security"));
    public static final StreamCodec<ByteBuf, PacketSyncSecurity> STREAM_CODEC = StreamCodec.composite(
          UUIDUtil.STREAM_CODEC, PacketSyncSecurity::playerUUID,
          ByteBufCodecs.stringUtf8(PacketUtils.LAST_USERNAME_LENGTH), PacketSyncSecurity::playerUsername,
          ByteBufCodecs.optional(SecurityData.STREAM_CODEC), packet -> Optional.ofNullable(packet.securityData),
          (uuid, name, data) -> new PacketSyncSecurity(uuid, name, data.orElse(null))
    );

    public PacketSyncSecurity(SecurityFrequency frequency) {
        this(frequency.getOwner(), frequency.getOwnerName(), new SecurityData(frequency));
    }

    public PacketSyncSecurity(UUID uuid) {
        this(uuid, MekanismUtils.getLastKnownUsername(uuid), null);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSyncSecurity> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        MekanismClient.clientUUIDMap.put(playerUUID, playerUsername);
        if (securityData != null) {
            MekanismClient.clientSecurityMap.put(playerUUID, securityData);
        }
    }
}