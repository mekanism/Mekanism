package mekanism.common.network;

import java.util.function.Supplier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketUpdateInventorySlot {

    private final ItemStack containerStack;
    private final int slotId;

    public PacketUpdateInventorySlot(ItemStack containerStack, int slotId) {
        this.containerStack = containerStack;
        this.slotId = slotId;
    }

    public static void handle(PacketUpdateInventorySlot message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            if (player != null) {
                player.inventory.setInventorySlotContents(message.slotId, message.containerStack);
                player.inventory.markDirty();
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketUpdateInventorySlot pkt, PacketBuffer buf) {
        buf.writeItemStack(pkt.containerStack);
        buf.writeVarInt(pkt.slotId);
    }

    public static PacketUpdateInventorySlot decode(PacketBuffer buf) {
        return new PacketUpdateInventorySlot(buf.readItemStack(), buf.readVarInt());
    }
}
