package mekanism.common.network.to_client.security;

import java.util.UUID;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PacketSyncSecurity(UUID playerUUID, String playerUsername, @Nullable SecurityData securityData) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("sync_security");

    public PacketSyncSecurity(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readUtf(PacketUtils.LAST_USERNAME_LENGTH), buffer.readNullable(SecurityData::read));
    }

    public PacketSyncSecurity(SecurityFrequency frequency) {
        this(frequency.getOwner(), frequency.getOwnerName(), new SecurityData(frequency));
    }

    public PacketSyncSecurity(UUID uuid) {
        this(uuid, MekanismUtils.getLastKnownUsername(uuid), null);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        MekanismClient.clientUUIDMap.put(playerUUID, playerUsername);
        if (securityData != null) {
            MekanismClient.clientSecurityMap.put(playerUUID, securityData);
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeUtf(playerUsername, PacketUtils.LAST_USERNAME_LENGTH);
        buffer.writeNullable(securityData, (buf, data) -> data.write(buf));
    }
}