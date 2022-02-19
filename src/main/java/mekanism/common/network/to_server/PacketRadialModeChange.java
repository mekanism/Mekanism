package mekanism.common.network.to_server;

import mekanism.common.item.interfaces.IRadialModeItem;
import mekanism.common.item.interfaces.IRadialSelectorEnum;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketRadialModeChange implements IMekanismPacket {

    private final EquipmentSlotType slot;
    private final int change;

    public PacketRadialModeChange(EquipmentSlotType slot, int change) {
        this.slot = slot;
        this.change = change;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IRadialModeItem) {
                setMode(stack, (IRadialModeItem<?>) stack.getItem(), player);
            }
        }
    }

    public <TYPE extends Enum<TYPE> & IRadialSelectorEnum<TYPE>> void setMode(ItemStack stack, IRadialModeItem<TYPE> item, PlayerEntity player) {
        item.setMode(stack, player, item.getModeByIndex(change));
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(slot);
        buffer.writeVarInt(change);
    }

    public static PacketRadialModeChange decode(PacketBuffer buffer) {
        return new PacketRadialModeChange(buffer.readEnum(EquipmentSlotType.class), buffer.readVarInt());
    }
}
