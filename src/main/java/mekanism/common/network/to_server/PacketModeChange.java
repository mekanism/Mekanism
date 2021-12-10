package mekanism.common.network.to_server;

import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketModeChange implements IMekanismPacket {

    private final boolean displayChangeMessage;
    private final EquipmentSlotType slot;
    private final int shift;

    public PacketModeChange(EquipmentSlotType slot, boolean holdingShift) {
        this(slot, holdingShift ? -1 : 1, true);
    }

    public PacketModeChange(EquipmentSlotType slot, int shift) {
        this(slot, shift, false);
    }

    private PacketModeChange(EquipmentSlotType slot, int shift, boolean displayChangeMessage) {
        this.slot = slot;
        this.shift = shift;
        this.displayChangeMessage = displayChangeMessage;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IModeItem) {
                ((IModeItem) stack.getItem()).changeMode(player, stack, shift, displayChangeMessage);
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(slot);
        buffer.writeVarInt(shift);
        buffer.writeBoolean(displayChangeMessage);
    }

    public static PacketModeChange decode(PacketBuffer buffer) {
        return new PacketModeChange(buffer.readEnum(EquipmentSlotType.class), buffer.readVarInt(), buffer.readBoolean());
    }
}