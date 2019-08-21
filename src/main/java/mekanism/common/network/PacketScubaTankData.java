package mekanism.common.network;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.gear.ItemScubaTank;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketScubaTankData {

    private ScubaTankPacket packetType;
    private Set<UUID> activeGasmasks;
    private boolean value;
    private UUID uuid;

    private PacketScubaTankData(ScubaTankPacket type) {
        packetType = type;
    }

    public static PacketScubaTankData MODE_CHANGE(boolean change) {
        PacketScubaTankData m = new PacketScubaTankData(ScubaTankPacket.MODE);
        m.value = change;
        return m;
    }

    public static PacketScubaTankData UPDATE(UUID uuid, boolean state) {
        PacketScubaTankData m = new PacketScubaTankData(ScubaTankPacket.UPDATE);
        m.uuid = uuid;
        m.value = state;
        return m;
    }

    public static PacketScubaTankData FULL(Set<UUID> activeNames) {
        PacketScubaTankData m = new PacketScubaTankData(ScubaTankPacket.FULL);
        m.activeGasmasks = activeNames;
        return m;
    }

    public static void handle(PacketScubaTankData message, Supplier<Context> context) {
        // Queue up processing on the central thread
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (message.packetType == ScubaTankPacket.UPDATE) {
                Mekanism.playerState.setGasmaskState(message.uuid, message.value, false);
                // If we got this on the server, relay out to all players in the same dimension
                // TODO: Why is this a dimensional thing?!
                // because we dont send a packet when a player starts tracking another player (net.minecraftforge.event.entity.player.PlayerEvent.StartTracking)
                if (!player.world.isRemote) {
                    Mekanism.packetHandler.sendToDimension(message, player.world.getDimension().getType());
                }
            } else if (message.packetType == ScubaTankPacket.MODE) {
                // Use has changed the mode of their gasmask; update it
                ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemScubaTank) {
                    ((ItemScubaTank) stack.getItem()).toggleFlowing(stack);
                }
            } else if (message.packetType == ScubaTankPacket.FULL) {
                // This is a full sync; merge into our player state
                Mekanism.playerState.setActiveGasmasks(message.activeGasmasks);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketScubaTankData pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        if (pkt.packetType == ScubaTankPacket.MODE) {
            buf.writeBoolean(pkt.value);
        } else if (pkt.packetType == ScubaTankPacket.UPDATE) {
            buf.writeUniqueId(pkt.uuid);
            buf.writeBoolean(pkt.value);
        } else if (pkt.packetType == ScubaTankPacket.FULL) {
            buf.writeInt(pkt.activeGasmasks.size());
            for (UUID uuid : pkt.activeGasmasks) {
                buf.writeUniqueId(uuid);
            }
        }
    }

    public static PacketScubaTankData decode(PacketBuffer buf) {
        PacketScubaTankData packet = new PacketScubaTankData(buf.readEnumValue(ScubaTankPacket.class));
        if (packet.packetType == ScubaTankPacket.MODE) {
            packet.value = buf.readBoolean();
        } else if (packet.packetType == ScubaTankPacket.UPDATE) {
            packet.uuid = buf.readUniqueId();
            packet.value = buf.readBoolean();
        } else if (packet.packetType == ScubaTankPacket.FULL) {
            packet.activeGasmasks = new HashSet<>();
            int amount = buf.readInt();
            for (int i = 0; i < amount; i++) {
                packet.activeGasmasks.add(buf.readUniqueId());
            }
        }
        return packet;
    }

    public enum ScubaTankPacket {
        UPDATE,
        FULL,
        MODE
    }
}