package mekanism.common.network.to_server.qio;

import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketQIOClearCraftingWindow(byte window, boolean toPlayerInv) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("clear_qio");

    public PacketQIOClearCraftingWindow(FriendlyByteBuf buffer) {
        this(buffer.readByte(), buffer.readBoolean());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Player player = context.player().orElse(null);
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
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeByte(window);
        buffer.writeBoolean(toPlayerInv);
    }
}