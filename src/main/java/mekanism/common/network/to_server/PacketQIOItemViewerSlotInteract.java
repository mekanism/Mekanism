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
            if (freq != null) {
                if (type == Type.PUT) {
                    ItemStack curStack = player.containerMenu.getCarried();
                    //Count should always be greater than zero but validate against invalid packets
                    if (!curStack.isEmpty() && count > 0) {
                        ItemStack toAdd;
                        if (count < curStack.getCount()) {//Only adding part of the stack
                            toAdd = StackUtils.size(curStack, count);
                        } else {//Try to add the full held stack
                            toAdd = curStack;
                        }
                        ItemStack rejects = freq.addItem(toAdd);
                        //Calculate actual amount we were able to add of what we tried to add
                        int placed = toAdd.getCount() - rejects.getCount();
                        if (placed > 0) {
                            //If we added any from the held stack, shrink the held stack and update it on the client
                            curStack.shrink(placed);
                            updateCarried(player, container);
                        }
                    }
                } else {
                    HashedItem itemType = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(typeUUID);
                    if (itemType != null) {
                        if (type == Type.TAKE) {
                            ItemStack curStack = player.containerMenu.getCarried();
                            //Clamp amount to extract by max stack size in case something is wrong with the packet that got sent
                            // or multiple packets got sent before the server's response got to the client
                            //Note: Rather than checking if the cur stack is empty to know whether to grab the max stack size from it,
                            // we just assume they are the same type, as we will validate the type matches before actually extracting
                            int toRemove = Math.min(count, itemType.getMaxStackSize() - curStack.getCount());
                            //Check to make sure we actually have room in the carried stack for any more items
                            //Note: The current stack and the grabbed stack should always be stackable unless the client sent multiple packets
                            // before processing our response to the first one, but we need to validate it to make sure it can actually stack
                            // so that we can avoid accidentally voiding any items
                            if (toRemove > 0 && InventoryUtils.areItemsStackable(curStack, itemType.getInternalStack())) {
                                ItemStack extracted = freq.removeByType(itemType, toRemove);
                                if (!extracted.isEmpty()) {
                                    if (curStack.isEmpty()) {
                                        player.containerMenu.setCarried(extracted);
                                    } else {
                                        curStack.grow(extracted.getCount());
                                    }
                                    updateCarried(player, container);
                                }
                            }
                        } else if (type == Type.SHIFT_TAKE) {
                            ItemStack maxExtract = itemType.createStack(itemType.getMaxStackSize());
                            //Simulate how much room we have in the player's inventory before trying to extract anything from the frequency
                            ItemStack simulatedExcess = container.simulateInsertIntoPlayerInventory(player.getUUID(), maxExtract);
                            //Extract a stack, or as much as the inventory has room for if it can't fit a full stack
                            ItemStack extracted = freq.removeByType(itemType, maxExtract.getCount() - simulatedExcess.getCount());
                            if (!extracted.isEmpty()) {
                                ItemStack remainder = container.insertIntoPlayerInventory(player.getUUID(), extracted);
                                //In theory this should never fail as we simulate above to make sure we don't try moving more than we can
                                // but validate it just in case and handle it gracefully
                                if (!remainder.isEmpty()) {
                                    remainder = freq.addItem(remainder);
                                    if (!remainder.isEmpty()) {
                                        //Something went wrong, and we couldn't add it back into the frequency after just removing
                                        // log an error and just drop the item on the ground to avoid voiding it
                                        Mekanism.logger.error("QIO shift-click transfer resulted in lost items ({}). This shouldn't happen!", remainder);
                                        player.drop(remainder, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateCarried(ServerPlayer player, QIOItemViewerContainer container) {
        player.connection.send(new ClientboundContainerSetSlotPacket(-1, container.incrementStateId(), -1, player.containerMenu.getCarried()));
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
