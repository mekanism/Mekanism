package mekanism.common.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.security.SecurityData;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketSecurityUpdate {

    private SecurityPacket packetType;
    private SecurityData securityData;
    private String playerUsername;
    private UUID playerUUID;

    public PacketSecurityUpdate(SecurityPacket type, UUID uuid, SecurityData data) {
        packetType = type;
        if (packetType == SecurityPacket.UPDATE) {
            playerUUID = uuid;
            playerUsername = MekanismUtils.getLastKnownUsername(uuid);
            securityData = data;
        }
    }

    private PacketSecurityUpdate(SecurityPacket type) {
        packetType = type;
    }

    public static void handle(PacketSecurityUpdate message, Supplier<Context> context) {
        if (message.packetType == SecurityPacket.UPDATE) {
            if (message.securityData != null) {
                MekanismClient.clientSecurityMap.put(message.playerUUID, message.securityData);
            }
        }
    }

    public static void encode(PacketSecurityUpdate pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        if (pkt.packetType == SecurityPacket.UPDATE) {
            buf.writeUniqueId(pkt.playerUUID);
            buf.writeString(pkt.playerUsername);
            if (pkt.securityData != null) {
                buf.writeBoolean(true);
                pkt.securityData.write(buf);
            } else {
                buf.writeBoolean(false);
            }
        } else if (pkt.packetType == SecurityPacket.FULL) {
            List<SecurityFrequency> frequencies = new ArrayList<>();
            for (Frequency frequency : Mekanism.securityFrequencies.getFrequencies()) {
                if (frequency instanceof SecurityFrequency) {
                    frequencies.add((SecurityFrequency) frequency);
                }
            }
            buf.writeInt(frequencies.size());
            for (SecurityFrequency frequency : frequencies) {
                buf.writeUniqueId(frequency.ownerUUID);
                buf.writeString(MekanismUtils.getLastKnownUsername(frequency.ownerUUID));
                new SecurityData(frequency).write(buf);
            }
        }
    }

    public static PacketSecurityUpdate decode(PacketBuffer buf) {
        PacketSecurityUpdate packet = new PacketSecurityUpdate(buf.readEnumValue(SecurityPacket.class));
        if (packet.packetType == SecurityPacket.UPDATE) {
            packet.playerUUID = buf.readUniqueId();
            packet.playerUsername = buf.readString();
            if (buf.readBoolean()) {
                packet.securityData = SecurityData.read(buf);
            }
            MekanismClient.clientUUIDMap.put(packet.playerUUID, packet.playerUsername);
        } else if (packet.packetType == SecurityPacket.FULL) {
            MekanismClient.clientSecurityMap.clear();
            int amount = buf.readInt();
            for (int i = 0; i < amount; i++) {
                UUID uuid = buf.readUniqueId();
                String username = buf.readString();
                MekanismClient.clientSecurityMap.put(uuid, SecurityData.read(buf));
                MekanismClient.clientUUIDMap.put(uuid, username);
            }
        }
        return packet;
    }

    public enum SecurityPacket {
        UPDATE,
        FULL
    }
}