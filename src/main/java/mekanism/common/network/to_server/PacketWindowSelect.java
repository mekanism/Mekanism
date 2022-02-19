package mekanism.common.network.to_server;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketWindowSelect implements IMekanismPacket {

    @Nullable
    private final SelectedWindowData selectedWindow;

    public PacketWindowSelect(@Nullable SelectedWindowData selectedWindow) {
        this.selectedWindow = selectedWindow;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player != null && player.containerMenu instanceof MekanismContainer) {
            ((MekanismContainer) player.containerMenu).setSelectedWindow(player.getUUID(), selectedWindow);
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        //We skip using a boolean to write if it is null or not and just write our byte before our enum so that we can
        // use -1 as a marker for invalid
        if (selectedWindow == null) {
            buffer.writeByte(-1);
        } else {
            buffer.writeByte(selectedWindow.extraData);
            buffer.writeEnum(selectedWindow.type);
        }
    }

    public static PacketWindowSelect decode(PacketBuffer buffer) {
        byte extraData = buffer.readByte();
        if (extraData == -1) {
            return new PacketWindowSelect(null);
        }
        WindowType windowType = buffer.readEnum(WindowType.class);
        return new PacketWindowSelect(windowType == WindowType.UNSPECIFIED ? SelectedWindowData.UNSPECIFIED : new SelectedWindowData(windowType, extraData));
    }
}