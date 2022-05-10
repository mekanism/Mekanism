package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketModeChangeCurios implements IMekanismPacket {

    private final boolean displayChangeMessage;
    private final String slotType;
    private final int slot;
    private final int shift;

    public PacketModeChangeCurios(String slotType, int slot, boolean holdingShift) {
        this(slotType, slot, holdingShift ? -1 : 1, true);
    }

    public PacketModeChangeCurios(String slotType, int slot, int shift) {
        this(slotType, slot, shift, false);
    }

    private PacketModeChangeCurios(String slotType, int slot, int shift, boolean displayChangeMessage) {
        this.slot = slot;
        this.shift = shift;
        this.slotType = slotType;
        this.displayChangeMessage = displayChangeMessage;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null && Mekanism.hooks.CuriosLoaded) {
            ItemStack stack = CuriosIntegration.getCurioStack(player, slotType, slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IModeItem modeItem) {
                modeItem.changeMode(player, stack, shift, displayChangeMessage);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(slotType);
        buffer.writeVarInt(slot);
        buffer.writeVarInt(shift);
        buffer.writeBoolean(displayChangeMessage);
    }

    public static PacketModeChangeCurios decode(FriendlyByteBuf buffer) {
        return new PacketModeChangeCurios(BasePacketHandler.readString(buffer), buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean());
    }
}