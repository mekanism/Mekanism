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
public class PacketJetpackData {

    private JetpackPacket packetType;
    private Set<UUID> activeJetpacks;
    private boolean value;
    private UUID uuid;

    private PacketJetpackData(JetpackPacket type) {
        packetType = type;
    }

    //Client to server AND sort of server to client, as the server then reroutes it to the client
    public static PacketJetpackData UPDATE(UUID uuid, boolean state) {
        PacketJetpackData m = new PacketJetpackData(JetpackPacket.UPDATE);
        m.uuid = uuid;
        m.value = state;
        return m;
    }

    //Server to client
    public static PacketJetpackData FULL(Set<UUID> activeNames) {
        PacketJetpackData m = new PacketJetpackData(JetpackPacket.FULL);
        m.activeJetpacks = activeNames;
        return m;
    }

    public static void handle(PacketJetpackData message, Supplier<Context> context) {
        // Queue up the processing on the central thread
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (message.packetType == JetpackPacket.UPDATE) {
                Mekanism.playerState.setJetpackState(message.uuid, message.value, false);
                // If we got this packet on the server, propagate it out to all players in the same
                // dimension
                // TODO: Why is this a dimensional thing?!
                // because we dont send a packet when a player starts tracking another player (net.minecraftforge.event.entity.player.PlayerEvent.StartTracking)
                if (!player.world.isRemote) {
                    Mekanism.packetHandler.sendToDimension(message, player.world.getDimension().getType());
                }
            } else if (message.packetType == JetpackPacket.FULL) {
                // This is a full sync; merge it into our player state
                Mekanism.playerState.setActiveJetpacks(message.activeJetpacks);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketJetpackData pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        if (pkt.packetType == JetpackPacket.UPDATE) {
            buf.writeUniqueId(pkt.uuid);
            buf.writeBoolean(pkt.value);
        } else if (pkt.packetType == JetpackPacket.FULL) {
            buf.writeInt(pkt.activeJetpacks.size());
            for (UUID uuid : pkt.activeJetpacks) {
                buf.writeUniqueId(uuid);
            }
        }
    }

    public static PacketJetpackData decode(PacketBuffer buf) {
        PacketJetpackData packet = new PacketJetpackData(buf.readEnumValue(JetpackPacket.class));
        if (packet.packetType == JetpackPacket.UPDATE) {
            packet.uuid = buf.readUniqueId();
            packet.value = buf.readBoolean();
        } else if (packet.packetType == JetpackPacket.FULL) {
            packet.activeJetpacks = new ObjectOpenHashSet<>();

            int amount = buf.readInt();
            for (int i = 0; i < amount; i++) {
                packet.activeJetpacks.add(buf.readUniqueId());
            }
        }
        return packet;
    }

    public enum JetpackPacket {
        UPDATE,
        FULL
    }
}