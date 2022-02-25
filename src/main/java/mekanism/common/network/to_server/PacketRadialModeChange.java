package mekanism.common.network.to_server;

import mekanism.common.item.interfaces.IRadialModeItem;
import mekanism.common.item.interfaces.IRadialSelectorEnum;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketRadialModeChange implements IMekanismPacket {

    private final EquipmentSlot slot;
    private final int change;

    public PacketRadialModeChange(EquipmentSlot slot, int change) {
        this.slot = slot;
        this.change = change;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IRadialModeItem<?> radialModeItem) {
                setMode(stack, radialModeItem, player);
            }
        }
    }

    public <TYPE extends Enum<TYPE> & IRadialSelectorEnum<TYPE>> void setMode(ItemStack stack, IRadialModeItem<TYPE> item, Player player) {
        item.setMode(stack, player, item.getModeByIndex(change));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(slot);
        buffer.writeVarInt(change);
    }

    public static PacketRadialModeChange decode(FriendlyByteBuf buffer) {
        return new PacketRadialModeChange(buffer.readEnum(EquipmentSlot.class), buffer.readVarInt());
    }
}
