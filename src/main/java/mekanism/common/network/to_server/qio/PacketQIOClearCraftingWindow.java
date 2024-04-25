package mekanism.common.network.to_server.qio;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketQIOClearCraftingWindow(byte window, boolean toPlayerInv) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketQIOClearCraftingWindow> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("clear_qio"));
    public static final StreamCodec<ByteBuf, PacketQIOClearCraftingWindow> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.BYTE, PacketQIOClearCraftingWindow::window,
          ByteBufCodecs.BOOL, PacketQIOClearCraftingWindow::toPlayerInv,
          PacketQIOClearCraftingWindow::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketQIOClearCraftingWindow> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        if (player.containerMenu instanceof QIOItemViewerContainer container) {
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
}