package mekanism.common.network;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketQIOItemViewerSlotInteract {

    private final Type type;
    private final UUID typeUUID;
    private final int count;

    private PacketQIOItemViewerSlotInteract(Type type, UUID typeUUID, int count) {
        this.type = type;
        this.typeUUID = typeUUID;
        this.count = count;
    }

    public static PacketQIOItemViewerSlotInteract take(UUID typeUUID, int count) {
        return new PacketQIOItemViewerSlotInteract(Type.TAKE, typeUUID, count);
    }

    public static PacketQIOItemViewerSlotInteract put(int count) {
        return new PacketQIOItemViewerSlotInteract(Type.PUT, null, count);
    }

    public static PacketQIOItemViewerSlotInteract shiftTake(UUID typeUUID) {
        return new PacketQIOItemViewerSlotInteract(Type.SHIFT_TAKE, typeUUID, 0);
    }

    public static void handle(PacketQIOItemViewerSlotInteract message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (player.openContainer instanceof QIOItemViewerContainer) {
                QIOItemViewerContainer container = (QIOItemViewerContainer) player.openContainer;
                QIOFrequency freq = container.getFrequency();
                ItemStack curStack = player.inventory.getItemStack();
                if (freq != null) {
                    if (message.type == Type.TAKE) {
                        ItemStack ret = freq.removeByType(freq.getTypeByUUID(message.typeUUID), message.count);
                        if (curStack.isEmpty()) {
                            player.inventory.setItemStack(ret);
                        } else if (InventoryUtils.areItemsStackable(ret, curStack)) {
                            curStack.grow(ret.getCount());
                        }
                        ((ServerPlayerEntity) player).connection.sendPacket(new SSetSlotPacket(-1, -1, player.inventory.getItemStack()));
                    } else if (message.type == Type.SHIFT_TAKE) {
                        HashedItem itemType = freq.getTypeByUUID(message.typeUUID);
                        if (itemType != null) {
                            ItemStack ret = freq.removeByType(itemType, itemType.getStack().getMaxStackSize());
                            ItemStack remainder = container.insertIntoPlayerInventory(ret);
                            if (!remainder.isEmpty()) {
                                remainder = freq.addItem(remainder);
                                if (!remainder.isEmpty()) {
                                    Mekanism.logger.error("QIO shift-click transfer resulted in lost items ({}). This shouldn't happen!", remainder);
                                }
                            }
                        }
                    } else if (message.type == Type.PUT) {
                        if (!curStack.isEmpty()) {
                            ItemStack rejects = freq.addItem(StackUtils.size(curStack, Math.min(message.count, curStack.getCount())));
                            ItemStack newStack = StackUtils.size(curStack, curStack.getCount() - (message.count - rejects.getCount()));
                            player.inventory.setItemStack(newStack);
                        }
                        ((ServerPlayerEntity) player).connection.sendPacket(new SSetSlotPacket(-1, -1, player.inventory.getItemStack()));
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketQIOItemViewerSlotInteract pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.type);
        switch (pkt.type) {
            case TAKE:
                buf.writeUniqueId(pkt.typeUUID);
                buf.writeVarInt(pkt.count);
                break;
            case SHIFT_TAKE:
                buf.writeUniqueId(pkt.typeUUID);
                break;
            case PUT:
                buf.writeVarInt(pkt.count);
                break;
        }
    }

    public static PacketQIOItemViewerSlotInteract decode(PacketBuffer buf) {
        Type type = buf.readEnumValue(Type.class);
        UUID typeUUID = null;
        int count = 0;
        switch (type) {
            case TAKE:
                typeUUID = buf.readUniqueId();
                count = buf.readVarInt();
                break;
            case SHIFT_TAKE:
                typeUUID = buf.readUniqueId();
                break;
            case PUT:
                count = buf.readVarInt();
                break;
        }
        return new PacketQIOItemViewerSlotInteract(type, typeUUID, count);
    }

    public enum Type {
        TAKE,
        SHIFT_TAKE,
        PUT;
    }
}
