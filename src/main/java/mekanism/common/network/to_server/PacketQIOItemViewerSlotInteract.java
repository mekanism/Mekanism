package mekanism.common.network.to_server;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketQIOItemViewerSlotInteract implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null && player.containerMenu instanceof QIOItemViewerContainer container) {
            QIOFrequency freq = container.getFrequency();
            ItemStack curStack = player.containerMenu.getCarried();
            if (freq != null) {
                if (type == Type.PUT) {
                    if (!curStack.isEmpty()) {
                        ItemStack rejects = freq.addItem(StackUtils.size(curStack, Math.min(count, curStack.getCount())));
                        ItemStack newStack = StackUtils.size(curStack, curStack.getCount() - (count - rejects.getCount()));
                        player.containerMenu.setCarried(newStack);
                    }
                    player.connection.send(new ClientboundContainerSetSlotPacket(-1, container.incrementStateId(), -1, player.containerMenu.getCarried()));
                } else {
                    HashedItem itemType = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(typeUUID);
                    if (itemType != null) {
                        if (type == Type.TAKE) {
                            //Should always be true but validate it before actually removing from the QIO
                            if (InventoryUtils.areItemsStackable(curStack, itemType.getStack())) {
                                ItemStack ret = freq.removeByType(itemType, count);
                                if (curStack.isEmpty()) {
                                    player.containerMenu.setCarried(ret);
                                } else if (InventoryUtils.areItemsStackable(ret, curStack)) {
                                    curStack.grow(ret.getCount());
                                }
                                player.connection.send(new ClientboundContainerSetSlotPacket(-1, container.incrementStateId(), -1,
                                      player.containerMenu.getCarried()));
                            }
                        } else if (type == Type.SHIFT_TAKE) {
                            ItemStack ret = freq.removeByType(itemType, itemType.getStack().getMaxStackSize());
                            if (!ret.isEmpty()) {
                                ItemStack remainder = container.insertIntoPlayerInventory(player.getUUID(), ret);
                                if (!remainder.isEmpty()) {
                                    remainder = freq.addItem(remainder);
                                    if (!remainder.isEmpty()) {
                                        Mekanism.logger.error("QIO shift-click transfer resulted in lost items ({}). This shouldn't happen!", remainder);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
        switch (type) {
            case TAKE -> {
                buffer.writeUUID(typeUUID);
                buffer.writeVarInt(count);
            }
            case SHIFT_TAKE -> buffer.writeUUID(typeUUID);
            case PUT -> buffer.writeVarInt(count);
        }
    }

    public static PacketQIOItemViewerSlotInteract decode(FriendlyByteBuf buffer) {
        Type type = buffer.readEnum(Type.class);
        UUID typeUUID = null;
        int count = 0;
        switch (type) {
            case TAKE -> {
                typeUUID = buffer.readUUID();
                count = buffer.readVarInt();
            }
            case SHIFT_TAKE -> typeUUID = buffer.readUUID();
            case PUT -> count = buffer.readVarInt();
        }
        return new PacketQIOItemViewerSlotInteract(type, typeUUID, count);
    }

    public enum Type {
        TAKE,
        SHIFT_TAKE,
        PUT;
    }
}
