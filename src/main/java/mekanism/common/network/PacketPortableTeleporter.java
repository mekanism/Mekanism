package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.client.gui.item.GuiPortableTeleporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketPortableTeleporter {

    private final byte status;

    public PacketPortableTeleporter(byte status) {
        this.status = status;
    }

    public static void handle(PacketPortableTeleporter message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().currentScreen;
            if (screen instanceof GuiPortableTeleporter) {
                GuiPortableTeleporter teleporter = (GuiPortableTeleporter) screen;
                teleporter.setStatus(message.status);
                teleporter.updateButtons();
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketPortableTeleporter pkt, PacketBuffer buf) {
        buf.writeByte(pkt.status);
    }

    public static PacketPortableTeleporter decode(PacketBuffer buf) {
        return new PacketPortableTeleporter(buf.readByte());
    }
}