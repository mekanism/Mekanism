package mekanism.common.network;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.client.MekanismClient;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
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
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (message.isUpdate) {
                MekanismClient.clientUUIDMap.put(message.playerUUID, message.playerUsername);
                if (message.securityData != null) {
                    MekanismClient.clientSecurityMap.put(message.playerUUID, message.securityData);
                }
            } else {
                MekanismClient.clientSecurityMap.clear();
                message.securityMap.forEach(MekanismClient.clientSecurityMap::put);
                message.uuidMap.forEach(MekanismClient.clientUUIDMap::put);
            }
        });
        ctx.setPacketHandled(true);
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
            List<SecurityFrequency> frequencies = new ArrayList<>(FrequencyType.SECURITY.getManager(null).getFrequencies());
            buf.writeVarInt(frequencies.size());
            for (SecurityFrequency frequency : frequencies) {
                UUID owner = frequency.getOwner();
                //In theory I don't think we can ever get here if this is null
                buf.writeUniqueId(owner);
                new SecurityData(frequency).write(buf);
                buf.writeString(MekanismUtils.getLastKnownUsername(owner));
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