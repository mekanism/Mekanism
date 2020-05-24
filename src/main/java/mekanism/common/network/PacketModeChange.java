package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.item.interfaces.IModeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketModeChange {

    private final boolean displayChangeMessage;
    private final EquipmentSlotType slot;
    private final int shift;

    public PacketModeChange(EquipmentSlotType slot, boolean holdingShift) {
        this(slot, holdingShift ? -1 : 1, true);
    }

    public PacketModeChange(EquipmentSlotType slot, int shift) {
        this(slot, shift, false);
    }

    public PacketModeChange(EquipmentSlotType slot, int shift, boolean displayChangeMessage) {
        this.slot = slot;
        this.shift = shift;
        this.displayChangeMessage = displayChangeMessage;
    }

    public static void handle(PacketModeChange message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            ItemStack stack = player.getItemStackFromSlot(message.slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IModeItem) {
                ((IModeItem) stack.getItem()).changeMode(player, stack, message.shift, message.displayChangeMessage);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketModeChange pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.slot);
        buf.writeVarInt(pkt.shift);
        buf.writeBoolean(pkt.displayChangeMessage);
    }

    public static PacketModeChange decode(PacketBuffer buf) {
        return new PacketModeChange(buf.readEnumValue(EquipmentSlotType.class), buf.readVarInt(), buf.readBoolean());
    }
}