package mekanism.common.network.to_client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.client.MekanismClient;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSecurityUpdate implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        if (isUpdate) {
            MekanismClient.clientUUIDMap.put(playerUUID, playerUsername);
            if (securityData != null) {
                MekanismClient.clientSecurityMap.put(playerUUID, securityData);
            }
        } else {
            MekanismClient.clientSecurityMap.clear();
            MekanismClient.clientSecurityMap.putAll(securityMap);
            MekanismClient.clientUUIDMap.putAll(uuidMap);
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBoolean(isUpdate);
        if (isUpdate) {
            buffer.writeUUID(playerUUID);
            buffer.writeUtf(playerUsername);
            if (securityData == null) {
                buffer.writeBoolean(false);
            } else {
                buffer.writeBoolean(true);
                securityData.write(buffer);
            }
        } else {
            List<SecurityFrequency> frequencies = new ArrayList<>(FrequencyType.SECURITY.getManager(null).getFrequencies());
            //In theory no owner should be null but handle the case anyway just in case
            frequencies.removeIf(frequency -> frequency.getOwner() == null);
            buffer.writeVarInt(frequencies.size());
            for (SecurityFrequency frequency : frequencies) {
                UUID owner = frequency.getOwner();
                //We remove all null cases above
                buffer.writeUUID(owner);
                new SecurityData(frequency).write(buffer);
                buffer.writeUtf(MekanismUtils.getLastKnownUsername(owner));
            }
        }
    }

    public static PacketSecurityUpdate decode(PacketBuffer buffer) {
        PacketSecurityUpdate packet = new PacketSecurityUpdate(buffer.readBoolean());
        if (packet.isUpdate) {
            packet.playerUUID = buffer.readUUID();
            packet.playerUsername = BasePacketHandler.readString(buffer);
            if (buffer.readBoolean()) {
                packet.securityData = SecurityData.read(buffer);
            }
        } else {
            int frequencySize = buffer.readVarInt();
            packet.securityMap = new Object2ObjectOpenHashMap<>(frequencySize);
            packet.uuidMap = new Object2ObjectOpenHashMap<>(frequencySize);
            for (int i = 0; i < frequencySize; i++) {
                UUID uuid = buffer.readUUID();
                packet.securityMap.put(uuid, SecurityData.read(buffer));
                packet.uuidMap.put(uuid, BasePacketHandler.readString(buffer));
            }
        }
        return packet;
    }
}