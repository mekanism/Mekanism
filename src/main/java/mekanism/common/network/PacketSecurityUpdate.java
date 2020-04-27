package mekanism.common.network;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.security.SecurityData;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketSecurityUpdate {

    private final boolean isUpdate;
    //Sync
    private SecurityData securityData;
    private String playerUsername;
    private UUID playerUUID;
    //Batch
    private Map<UUID, SecurityData> securityMap = new Object2ObjectOpenHashMap<>();
    private Map<UUID, String> uuidMap = new Object2ObjectOpenHashMap<>();

    public PacketSecurityUpdate(UUID uuid, SecurityData data) {
        this(true);
        playerUUID = uuid;
        playerUsername = MekanismUtils.getLastKnownUsername(uuid);
        securityData = data;
    }

    public PacketSecurityUpdate() {
        this(false);
    }

    private PacketSecurityUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public static void handle(PacketSecurityUpdate message, Supplier<Context> context) {
        context.get().enqueueWork(() -> {
            if (message.isUpdate) {
                MekanismClient.clientUUIDMap.put(message.playerUUID, message.playerUsername);
                if (message.securityData != null) {
                    MekanismClient.clientSecurityMap.put(message.playerUUID, message.securityData);
                }
            } else {
                MekanismClient.clientSecurityMap.clear();
                message.securityMap.forEach((key, value) -> MekanismClient.clientSecurityMap.put(key, value));
                message.uuidMap.forEach((key, value) -> MekanismClient.clientUUIDMap.put(key, value));
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketSecurityUpdate pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.isUpdate);
        if (pkt.isUpdate) {
            buf.writeUniqueId(pkt.playerUUID);
            buf.writeString(pkt.playerUsername);
            if (pkt.securityData == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                pkt.securityData.write(buf);
            }
        } else {
            List<SecurityFrequency> frequencies = Mekanism.securityFrequencies.getPublicManager().getFrequencies().values().stream().collect(Collectors.toList());
            buf.writeVarInt(frequencies.size());
            for (SecurityFrequency frequency : frequencies) {
                buf.writeUniqueId(frequency.ownerUUID);
                new SecurityData(frequency).write(buf);
                buf.writeString(MekanismUtils.getLastKnownUsername(frequency.ownerUUID));
            }
        }
    }

    public static PacketSecurityUpdate decode(PacketBuffer buf) {
        PacketSecurityUpdate packet = new PacketSecurityUpdate(buf.readBoolean());
        if (packet.isUpdate) {
            packet.playerUUID = buf.readUniqueId();
            packet.playerUsername = BasePacketHandler.readString(buf);
            if (buf.readBoolean()) {
                packet.securityData = SecurityData.read(buf);
            }
        } else {
            int frequencySize = buf.readVarInt();
            packet.securityMap = new Object2ObjectOpenHashMap<>(frequencySize);
            packet.uuidMap = new Object2ObjectOpenHashMap<>(frequencySize);
            for (int i = 0; i < frequencySize; i++) {
                UUID uuid = buf.readUniqueId();
                packet.securityMap.put(uuid, SecurityData.read(buf));
                packet.uuidMap.put(uuid, BasePacketHandler.readString(buf));
            }
        }
        return packet;
    }
}