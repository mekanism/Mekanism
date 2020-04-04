package mekanism.common.network;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

//TODO: Re-evaluate/rewrite
public class PacketFlamethrowerData {

    private FlamethrowerPacket packetType;
    private Set<UUID> activeFlamethrowers;
    private boolean value;
    private UUID uuid;

    private PacketFlamethrowerData(FlamethrowerPacket type) {
        packetType = type;
    }

    public static PacketFlamethrowerData UPDATE(UUID uuid, boolean state) {
        PacketFlamethrowerData m = new PacketFlamethrowerData(FlamethrowerPacket.UPDATE);
        m.uuid = uuid;
        m.value = state;
        return m;
    }

    public static PacketFlamethrowerData FULL(Set<UUID> activeNames) {
        PacketFlamethrowerData m = new PacketFlamethrowerData(FlamethrowerPacket.FULL);
        m.activeFlamethrowers = activeNames;
        return m;
    }

    public static void handle(PacketFlamethrowerData message, Supplier<Context> context) {
        // Queue up the processing on the central thread
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (message.packetType == FlamethrowerPacket.UPDATE) {
                Mekanism.playerState.setFlamethrowerState(message.uuid, message.value, false);
                // If we got this packet on the server, resend out to all clients in same dimension
                // TODO: Why is this a dimensional thing?!
                // because we dont send a packet when a player starts tracking another player (net.minecraftforge.event.entity.player.PlayerEvent.StartTracking)
                if (!player.world.isRemote) {
                    Mekanism.packetHandler.sendToDimension(message, player.world.getDimension().getType());
                }
            } else if (message.packetType == FlamethrowerPacket.FULL) {
                // This is a full sync; merge into our player state
                Mekanism.playerState.setActiveFlamethrowers(message.activeFlamethrowers);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketFlamethrowerData pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        if (pkt.packetType == FlamethrowerPacket.UPDATE) {
            buf.writeUniqueId(pkt.uuid);
            buf.writeBoolean(pkt.value);
        } else if (pkt.packetType == FlamethrowerPacket.FULL) {
            buf.writeInt(pkt.activeFlamethrowers.size());
            for (UUID uuid : pkt.activeFlamethrowers) {
                buf.writeUniqueId(uuid);
            }
        }
    }

    public static PacketFlamethrowerData decode(PacketBuffer buf) {
        PacketFlamethrowerData packet = new PacketFlamethrowerData(buf.readEnumValue(FlamethrowerPacket.class));
        if (packet.packetType == FlamethrowerPacket.UPDATE) {
            packet.uuid = buf.readUniqueId();
            packet.value = buf.readBoolean();
        } else if (packet.packetType == FlamethrowerPacket.FULL) {
            packet.activeFlamethrowers = new ObjectOpenHashSet<>();
            int amount = buf.readInt();
            for (int i = 0; i < amount; i++) {
                packet.activeFlamethrowers.add(buf.readUniqueId());
            }
        }
        return packet;
    }

    public enum FlamethrowerPacket {
        UPDATE,
        FULL
    }
}