package mekanism.common.network.to_server;

import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketModeChange implements IMekanismPacket {

    private final boolean displayChangeMessage;
    private final EquipmentSlot slot;
    private final int shift;

    public PacketModeChange(EquipmentSlot slot, boolean holdingShift) {
        this(slot, holdingShift ? -1 : 1, true);
    }

    public PacketModeChange(EquipmentSlot slot, int shift) {
        this(slot, shift, false);
    }

    private PacketModeChange(EquipmentSlot slot, int shift, boolean displayChangeMessage) {
        this.slot = slot;
        this.shift = shift;
        this.displayChangeMessage = displayChangeMessage;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IModeItem modeItem) {
                modeItem.changeMode(player, stack, shift, displayChangeMessage);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(slot);
        buffer.writeVarInt(shift);
        buffer.writeBoolean(displayChangeMessage);
    }

    public static PacketModeChange decode(FriendlyByteBuf buffer) {
        return new PacketModeChange(buffer.readEnum(EquipmentSlot.class), buffer.readVarInt(), buffer.readBoolean());
    }
}