package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketQIOItemViewerSlotInteract {

    private Type type;
    private HashedItem itemType;
    private int count;

    private PacketQIOItemViewerSlotInteract(Type type, HashedItem itemType, int count) {
        this.type = type;
        this.itemType = itemType;
        this.count = count;
    }

    public static PacketQIOItemViewerSlotInteract take(HashedItem itemType, int count) {
        return new PacketQIOItemViewerSlotInteract(Type.TAKE, itemType, count);
    }

    public static PacketQIOItemViewerSlotInteract put(int count) {
        return new PacketQIOItemViewerSlotInteract(Type.PUT, null, count);
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
                        ItemStack ret = freq.removeByType(message.itemType, message.count);
                        if (curStack.isEmpty()) {
                            player.inventory.setItemStack(curStack);
                        } else if (InventoryUtils.areItemsStackable(ret, curStack)) {
                            curStack.grow(ret.getCount());
                        }
                        player.inventory.markDirty();
                    } else if (message.type == Type.PUT) {
                        if (!curStack.isEmpty()) {
                            player.inventory.setItemStack(freq.addItem(StackUtils.size(curStack, message.count)));
                            player.inventory.markDirty();
                        }
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
                buf.writeItemStack(pkt.itemType.getStack());
                buf.writeInt(pkt.count);
                break;
            case PUT:
                buf.writeInt(pkt.count);
                break;
        }
    }

    public static PacketQIOItemViewerSlotInteract decode(PacketBuffer buf) {
        Type type = buf.readEnumValue(Type.class);
        HashedItem item = null;
        int count = 0;
        switch (type) {
            case TAKE:
                item = new HashedItem(buf.readItemStack());
                count = buf.readInt();
                break;
            case PUT:
                count = buf.readInt();
                break;
        }
        return new PacketQIOItemViewerSlotInteract(type, item, count);
    }

    public enum Type {
        TAKE,
        PUT;
    }
}
