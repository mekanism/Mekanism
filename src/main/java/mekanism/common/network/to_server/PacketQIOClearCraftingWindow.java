package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PacketQIOClearCraftingWindow implements IMekanismPacket {

    private final byte window;
    private final boolean toPlayerInv;

    public PacketQIOClearCraftingWindow(byte window, boolean toPlayerInv) {
        this.window = window;
        this.toPlayerInv = toPlayerInv;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null && player.containerMenu instanceof QIOItemViewerContainer container) {
            byte selectedCraftingGrid = container.getSelectedCraftingGrid(player.getUUID());
            if (selectedCraftingGrid == -1) {
                Mekanism.logger.warn("Received clear request from: {}, but they do not currently have a crafting window open.", player);
            } else if (selectedCraftingGrid != window) {
                Mekanism.logger.warn("Received clear request from: {}, but they currently have a different crafting window open.", player);
            } else {
                QIOCraftingWindow craftingWindow = container.getCraftingWindow(selectedCraftingGrid);
                craftingWindow.emptyTo(toPlayerInv, container.getHotBarSlots(), container.getMainInventorySlots());
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeByte(window);
        buffer.writeBoolean(toPlayerInv);
    }

    public static PacketQIOClearCraftingWindow decode(FriendlyByteBuf buffer) {
        return new PacketQIOClearCraftingWindow(buffer.readByte(), buffer.readBoolean());
    }
}