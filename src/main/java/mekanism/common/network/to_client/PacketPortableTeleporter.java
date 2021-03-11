package mekanism.common.network.to_client;

import mekanism.client.gui.item.GuiPortableTeleporter;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPortableTeleporter implements IMekanismPacket {

    private final byte status;

    public PacketPortableTeleporter(byte status) {
        this.status = status;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiPortableTeleporter) {
            GuiPortableTeleporter teleporter = (GuiPortableTeleporter) screen;
            teleporter.setStatus(status);
            teleporter.updateButtons();
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeByte(status);
    }

    public static PacketPortableTeleporter decode(PacketBuffer buffer) {
        return new PacketPortableTeleporter(buffer.readByte());
    }
}