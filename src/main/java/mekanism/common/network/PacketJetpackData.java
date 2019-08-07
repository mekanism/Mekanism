package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketJetpackData {

    private JetpackPacket packetType;
    private Set<UUID> activeJetpacks;
    private boolean value;
    private UUID uuid;

    private PacketJetpackData(JetpackPacket type) {
        packetType = type;
    }

    public static PacketJetpackData MODE_CHANGE(boolean change) {
        PacketJetpackData m = new PacketJetpackData(JetpackPacket.MODE);
        m.value = change;
        return m;
    }

    public static PacketJetpackData UPDATE(UUID uuid, boolean state) {
        PacketJetpackData m = new PacketJetpackData(JetpackPacket.UPDATE);
        m.uuid = uuid;
        m.value = state;
        return m;
    }

    public static PacketJetpackData FULL(Set<UUID> activeNames) {
        PacketJetpackData m = new PacketJetpackData(JetpackPacket.FULL);
        m.activeJetpacks = activeNames;
        return m;
    }

    public static void handle(PacketJetpackData message, Supplier<Context> context) {
        // Queue up the processing on the central thread
        PlayerEntity player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            if (message.packetType == JetpackPacket.UPDATE) {
                Mekanism.playerState.setJetpackState(message.uuid, message.value, false);
                // If we got this packet on the server, propagate it out to all players in the same
                // dimension
                // TODO: Why is this a dimensional thing?!
                // because we dont send a packet when a player starts tracking another player (net.minecraftforge.event.entity.player.PlayerEvent.StartTracking)
                if (!player.world.isRemote) {
                    Mekanism.packetHandler.sendToDimension(message, player.world.provider.getDimension());
                }
            } else if (message.packetType == JetpackPacket.MODE) {
                // Use has changed the mode of their jetpack; update it
                ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemJetpack) {
                    if (!message.value) {
                        ((ItemJetpack) stack.getItem()).incrementMode(stack);
                    } else {
                        ((ItemJetpack) stack.getItem()).setMode(stack, JetpackMode.DISABLED);
                    }
                }
            } else if (message.packetType == JetpackPacket.FULL) {
                // This is a full sync; merge it into our player state
                Mekanism.playerState.setActiveJetpacks(message.activeJetpacks);
            }
        }, player);
    }

    public static void encode(PacketJetpackData pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        if (pkt.packetType == JetpackPacket.MODE) {
            buf.writeBoolean(pkt.value);
        } else if (pkt.packetType == JetpackPacket.UPDATE) {
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
        if (packet.packetType == JetpackPacket.MODE) {
            packet.value = buf.readBoolean();
        } else if (packet.packetType == JetpackPacket.UPDATE) {
            packet.uuid = buf.readUniqueId();
            packet.value = buf.readBoolean();
        } else if (packet.packetType == JetpackPacket.FULL) {
            packet.activeJetpacks = new HashSet<>();

            int amount = buf.readInt();
            for (int i = 0; i < amount; i++) {
                packet.activeJetpacks.add(buf.readUniqueId());
            }
        }
        return packet;
    }

    public enum JetpackPacket {
        UPDATE,
        FULL,
        MODE
    }
}