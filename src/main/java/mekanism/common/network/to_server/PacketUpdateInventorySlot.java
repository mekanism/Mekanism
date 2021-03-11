package mekanism.common.network.to_server;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUpdateInventorySlot implements IMekanismPacket {

    private final ItemStack containerStack;
    private final int slotId;

    public PacketUpdateInventorySlot(ItemStack containerStack, int slotId) {
        this.containerStack = containerStack;
        this.slotId = slotId;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            player.inventory.setItem(slotId, containerStack);
            player.inventory.setChanged();
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeItem(containerStack);
        buffer.writeVarInt(slotId);
    }

    public static PacketUpdateInventorySlot decode(PacketBuffer buffer) {
        return new PacketUpdateInventorySlot(buffer.readItem(), buffer.readVarInt());
    }
}
