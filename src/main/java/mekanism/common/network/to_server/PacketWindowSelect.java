package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PacketWindowSelect(@Nullable SelectedWindowData selectedWindow) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("window_select");

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Player player = context.player().orElse(null);
        if (player != null && player.containerMenu instanceof MekanismContainer container) {
            container.setSelectedWindow(player.getUUID(), selectedWindow);
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        //We skip using a boolean to write if it is null or not and just write our byte before our enum so that we can
        // use -1 as a marker for invalid
        if (selectedWindow == null) {
            buffer.writeByte(-1);
        } else {
            buffer.writeByte(selectedWindow.extraData);
            buffer.writeEnum(selectedWindow.type);
        }
    }

    public static PacketWindowSelect decode(FriendlyByteBuf buffer) {
        byte extraData = buffer.readByte();
        if (extraData == -1) {
            return new PacketWindowSelect(null);
        }
        WindowType windowType = buffer.readEnum(WindowType.class);
        return new PacketWindowSelect(windowType == WindowType.UNSPECIFIED ? SelectedWindowData.UNSPECIFIED : new SelectedWindowData(windowType, extraData));
    }
}