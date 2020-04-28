package mekanism.common.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.client.gui.GuiPortableTeleporter;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.frequency.Frequency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketPortableTeleporter {

    private List<TeleporterFrequency> publicCache;
    private List<TeleporterFrequency> privateCache;
    private TeleporterFrequency frequency;
    private Hand currentHand;
    private byte status;

    public PacketPortableTeleporter(Hand hand, TeleporterFrequency freq, byte b, List<TeleporterFrequency> publicFreqs, List<TeleporterFrequency> privateFreqs) {
        currentHand = hand;
        frequency = freq;
        status = b;

        publicCache = publicFreqs;
        privateCache = privateFreqs;
    }

    public static void handle(PacketPortableTeleporter message, Supplier<Context> context) {
        context.get().enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().currentScreen;
            if (screen instanceof GuiPortableTeleporter) {
                GuiPortableTeleporter teleporter = (GuiPortableTeleporter) screen;
                teleporter.setStatus(message.status);
                teleporter.setFrequency(message.frequency);
                teleporter.setPublicCache(message.publicCache);
                teleporter.setPrivateCache(message.privateCache);
                teleporter.updateButtons();
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketPortableTeleporter pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.currentHand);
        if (pkt.frequency == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeString(pkt.frequency.getName());
            buf.writeBoolean(pkt.frequency.isPublic());
        }
        buf.writeByte(pkt.status);
        buf.writeVarInt(pkt.publicCache.size());
        for (TeleporterFrequency freq : pkt.publicCache) {
            freq.write(buf);
        }
        buf.writeVarInt(pkt.privateCache.size());
        for (TeleporterFrequency freq : pkt.privateCache) {
            freq.write(buf);
        }
    }

    public static PacketPortableTeleporter decode(PacketBuffer buf) {
        Hand currentHand = buf.readEnumValue(Hand.class);
        List<TeleporterFrequency> publicCache = new ArrayList<>();
        List<TeleporterFrequency> privateCache = new ArrayList<>();
        TeleporterFrequency frequency = null;
        if (buf.readBoolean()) {
            frequency = new TeleporterFrequency(BasePacketHandler.readString(buf), null);
            frequency.setPublic(buf.readBoolean());
        }
        byte status = buf.readByte();
        int amount = buf.readVarInt();
        for (int i = 0; i < amount; i++) {
            publicCache.add((TeleporterFrequency) Frequency.readFromPacket(buf));
        }
        amount = buf.readVarInt();
        for (int i = 0; i < amount; i++) {
            privateCache.add((TeleporterFrequency) Frequency.readFromPacket(buf));
        }
        return new PacketPortableTeleporter(currentHand, frequency, status, publicCache, privateCache);
    }
}