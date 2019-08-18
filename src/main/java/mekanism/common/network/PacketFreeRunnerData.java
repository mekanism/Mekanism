package mekanism.common.network;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.gear.ItemFreeRunners;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketFreeRunnerData {

    private FreeRunnerPacket packetType;
    private boolean value;
    private UUID uuid;

    public PacketFreeRunnerData(FreeRunnerPacket packetType, UUID uuid, boolean value) {
        this.packetType = packetType;
        this.value = value;
        if (packetType == FreeRunnerPacket.UPDATE) {
            this.uuid = uuid;
        }
    }

    public static void handle(PacketFreeRunnerData message, Supplier<Context> context) {
        PlayerEntity entityPlayer = PacketHandler.getPlayer(context);

        PacketHandler.handlePacket(() -> {
            if (message.packetType == FreeRunnerPacket.UPDATE) {
                if (message.value) {
                    Mekanism.freeRunnerOn.add(message.uuid);
                } else {
                    Mekanism.freeRunnerOn.remove(message.uuid);
                }
                if (!entityPlayer.world.isRemote) {
                    Mekanism.packetHandler.sendToDimension(new PacketFreeRunnerData(FreeRunnerPacket.UPDATE, message.uuid, message.value), entityPlayer.world.getDimension().getType());
                }
            } else if (message.packetType == FreeRunnerPacket.MODE) {
                ItemStack stack = entityPlayer.getItemStackFromSlot(EquipmentSlotType.FEET);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemFreeRunners) {
                    if (!message.value) {
                        ((ItemFreeRunners) stack.getItem()).incrementMode(stack);
                    } else {
                        ((ItemFreeRunners) stack.getItem()).setMode(stack, ItemFreeRunners.FreeRunnerMode.DISABLED);
                    }
                }
            }
        }, entityPlayer);
    }

    public static void encode(PacketFreeRunnerData pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        if (pkt.packetType == FreeRunnerPacket.MODE) {
            buf.writeBoolean(pkt.value);
        } else if (pkt.packetType == FreeRunnerPacket.UPDATE) {
            buf.writeUniqueId(pkt.uuid);
            buf.writeBoolean(pkt.value);
        } else if (pkt.packetType == FreeRunnerPacket.FULL) {
            buf.writeInt(Mekanism.freeRunnerOn.size());
            synchronized (Mekanism.freeRunnerOn) {
                for (UUID uuidToSend : Mekanism.freeRunnerOn) {
                    buf.writeUniqueId(uuidToSend);
                }
            }
        }
    }

    public static PacketFreeRunnerData decode(PacketBuffer buf) {
        FreeRunnerPacket packetType = buf.readEnumValue(FreeRunnerPacket.class);
        boolean value = false;
        UUID uuid = null;
        if (packetType == FreeRunnerPacket.MODE) {
            value = buf.readBoolean();
        } else if (packetType == FreeRunnerPacket.UPDATE) {
            uuid = buf.readUniqueId();
            value = buf.readBoolean();
        } else if (packetType == FreeRunnerPacket.FULL) {
            Mekanism.freeRunnerOn.clear();
            int amount = buf.readInt();
            for (int i = 0; i < amount; i++) {
                Mekanism.freeRunnerOn.add(buf.readUniqueId());
            }
        }
        return new PacketFreeRunnerData(packetType, uuid, value);
    }

    public enum FreeRunnerPacket {
        UPDATE,
        FULL,
        MODE
    }
}