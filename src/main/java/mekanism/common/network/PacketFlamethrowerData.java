package mekanism.common.network;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.gear.ItemFlamethrower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketFlamethrowerData {

    private FlamethrowerPacket packetType;
    private Set<UUID> activeFlamethrowers;
    private Hand currentHand;
    private boolean value;
    private UUID uuid;

    private PacketFlamethrowerData(FlamethrowerPacket type) {
        packetType = type;
    }

    public static PacketFlamethrowerData MODE_CHANGE(Hand hand) {
        PacketFlamethrowerData m = new PacketFlamethrowerData(FlamethrowerPacket.MODE);
        m.currentHand = hand;
        return m;
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
            } else if (message.packetType == FlamethrowerPacket.MODE) {
                ItemStack stack = player.getHeldItem(message.currentHand);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemFlamethrower) {
                    ((ItemFlamethrower) stack.getItem()).incrementMode(stack);
                }
            } else if (message.packetType == FlamethrowerPacket.FULL) {
                // This is a full sync; merge into our player state
                Mekanism.playerState.setActiveFlamethrowers(message.activeFlamethrowers);
            }
        });
    }

    public static void encode(PacketFlamethrowerData pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        if (pkt.packetType == FlamethrowerPacket.UPDATE) {
            buf.writeUniqueId(pkt.uuid);
            buf.writeBoolean(pkt.value);
        } else if (pkt.packetType == FlamethrowerPacket.MODE) {
            buf.writeEnumValue(pkt.currentHand);
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
        } else if (packet.packetType == FlamethrowerPacket.MODE) {
            packet.currentHand = buf.readEnumValue(Hand.class);
        } else if (packet.packetType == FlamethrowerPacket.FULL) {
            packet.activeFlamethrowers = new HashSet<>();
            int amount = buf.readInt();
            for (int i = 0; i < amount; i++) {
                packet.activeFlamethrowers.add(buf.readUniqueId());
            }
        }
        return packet;
    }

    public enum FlamethrowerPacket {
        UPDATE,
        FULL,
        MODE
    }
}