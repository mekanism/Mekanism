package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.frequency.Frequency;
import mekanism.common.network.PacketSecurityUpdate.SecurityUpdateMessage;
import mekanism.common.security.SecurityData;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSecurityUpdate implements IMessageHandler<SecurityUpdateMessage, IMessage> {

    @Override
    public IMessage onMessage(SecurityUpdateMessage message, MessageContext context) {
        if (message.packetType == SecurityPacket.UPDATE) {
            if (message.securityData != null) {
                MekanismClient.clientSecurityMap.put(message.playerUUID, message.securityData);
            }
        }

        return null;
    }

    public enum SecurityPacket {
        UPDATE,
        FULL
    }

    public static class SecurityUpdateMessage implements IMessage {

        public SecurityPacket packetType;

        public UUID playerUUID;
        public String playerUsername;
        public SecurityData securityData;

        public SecurityUpdateMessage() {
        }

        public SecurityUpdateMessage(SecurityPacket type, UUID uuid, SecurityData data) {
            packetType = type;

            if (packetType == SecurityPacket.UPDATE) {
                playerUUID = uuid;
                playerUsername = MekanismUtils.getLastKnownUsername(uuid);
                securityData = data;
            }
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(packetType.ordinal());

            if (packetType == SecurityPacket.UPDATE) {
                PacketHandler.writeString(dataStream, playerUUID.toString());
                PacketHandler.writeString(dataStream, playerUsername);

                if (securityData != null) {
                    dataStream.writeBoolean(true);
                    securityData.write(dataStream);
                } else {
                    dataStream.writeBoolean(false);
                }
            } else if (packetType == SecurityPacket.FULL) {
                List<SecurityFrequency> frequencies = new ArrayList<>();

                for (Frequency frequency : Mekanism.securityFrequencies.getFrequencies()) {
                    if (frequency instanceof SecurityFrequency) {
                        frequencies.add((SecurityFrequency) frequency);
                    }
                }

                dataStream.writeInt(frequencies.size());

                for (SecurityFrequency frequency : frequencies) {
                    PacketHandler.writeString(dataStream, frequency.ownerUUID.toString());
                    PacketHandler.writeString(dataStream, MekanismUtils.getLastKnownUsername(frequency.ownerUUID));
                    new SecurityData(frequency).write(dataStream);
                }
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            packetType = SecurityPacket.values()[dataStream.readInt()];

            if (packetType == SecurityPacket.UPDATE) {
                playerUUID = UUID.fromString(PacketHandler.readString(dataStream));
                playerUsername = PacketHandler.readString(dataStream);

                if (dataStream.readBoolean()) {
                    securityData = SecurityData.read(dataStream);
                }

                MekanismClient.clientUUIDMap.put(playerUUID, playerUsername);
            } else if (packetType == SecurityPacket.FULL) {
                MekanismClient.clientSecurityMap.clear();

                int amount = dataStream.readInt();

                for (int i = 0; i < amount; i++) {
                    UUID uuid = UUID.fromString(PacketHandler.readString(dataStream));
                    String username = PacketHandler.readString(dataStream);
                    MekanismClient.clientSecurityMap.put(uuid, SecurityData.read(dataStream));
                    MekanismClient.clientUUIDMap.put(uuid, username);
                }
            }
        }
    }
}
