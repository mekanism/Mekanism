package mekanism.common.network.to_server;

import mekanism.common.item.interfaces.ISaveModeItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketSaveMode implements IMekanismPacket {

    private final int slotId;
    private final int modeId;

    public PacketSaveMode(int slotId, int modeId) {
        this.slotId = slotId;
        this.modeId = modeId;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            ItemStack stack = player.getInventory().getItem(slotId);
            if (!stack.isEmpty() && stack.getItem() instanceof ISaveModeItem saveModeItem) {
                saveModeItem.saveMode(stack, modeId);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(slotId);
        buffer.writeVarInt(modeId);
    }

    public static PacketSaveMode decode(FriendlyByteBuf buffer) {
        return new PacketSaveMode(buffer.readVarInt(), buffer.readVarInt());
    }
}