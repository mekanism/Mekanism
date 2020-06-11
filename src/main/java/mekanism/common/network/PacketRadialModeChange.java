package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.item.interfaces.IRadialModeItem;
import mekanism.common.item.interfaces.IRadialSelectorEnum;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketRadialModeChange {

    private final EquipmentSlotType slot;
    private final int change;

    public PacketRadialModeChange(EquipmentSlotType slot, int change) {
        this.slot = slot;
        this.change = change;
    }

    public static void handle(PacketRadialModeChange message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            ItemStack stack = player.getItemStackFromSlot(message.slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IRadialModeItem) {
                setMode(stack, (IRadialModeItem<?>) stack.getItem(), player, message.change);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static <TYPE extends Enum<TYPE> & IRadialSelectorEnum<TYPE>> void setMode(ItemStack stack, IRadialModeItem<TYPE> item, PlayerEntity player, int index) {
        item.setMode(stack, player, item.getModeByIndex(index));
    }

    public static void encode(PacketRadialModeChange pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.slot);
        buf.writeVarInt(pkt.change);
    }

    public static PacketRadialModeChange decode(PacketBuffer buf) {
        return new PacketRadialModeChange(buf.readEnumValue(EquipmentSlotType.class), buf.readVarInt());
    }
}
