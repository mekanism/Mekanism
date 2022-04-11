package mekanism.common.network.to_server;

import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketModeChangeCurios implements IMekanismPacket {

    private final boolean displayChangeMessage;
    private final int slot;
    private final int shift;

    public PacketModeChangeCurios(int slot, boolean holdingShift) {
        this(slot, holdingShift ? -1 : 1, true);
    }

    public PacketModeChangeCurios(int slot, int shift) {
        this(slot, shift, false);
    }

    private PacketModeChangeCurios(int slot, int shift, boolean displayChangeMessage) {
        this.slot = slot;
        this.shift = shift;
        this.displayChangeMessage = displayChangeMessage;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            CuriosIntegration.getCuriosInventory(player).ifPresent(inv -> {
                final ItemStack stack = inv.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() instanceof IModeItem modeItem) {
                    modeItem.changeMode(player, stack, shift, displayChangeMessage);
                }
            });
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(slot);
        buffer.writeVarInt(shift);
        buffer.writeBoolean(displayChangeMessage);
    }

    public static PacketModeChangeCurios decode(FriendlyByteBuf buffer) {
        return new PacketModeChangeCurios(buffer.readInt(), buffer.readVarInt(), buffer.readBoolean());
    }
}