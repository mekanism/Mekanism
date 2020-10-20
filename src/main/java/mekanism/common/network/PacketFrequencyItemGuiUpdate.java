package mekanism.common.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.inventory.container.item.FrequencyItemContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketFrequencyItemGuiUpdate<FREQ extends Frequency> {

    private final List<FREQ> publicCache;
    private final List<FREQ> privateCache;
    private final FrequencyType<FREQ> frequencyType;
    private final FREQ frequency;
    private final Hand currentHand;

    private PacketFrequencyItemGuiUpdate(Hand hand, FrequencyType<FREQ> type, FREQ freq, List<FREQ> publicFreqs, List<FREQ> privateFreqs) {
        currentHand = hand;
        frequencyType = type;
        frequency = freq;

        publicCache = publicFreqs;
        privateCache = privateFreqs;
    }

    public static <FREQ extends Frequency> PacketFrequencyItemGuiUpdate<FREQ> update(Hand hand, FrequencyType<FREQ> type, UUID ownerUUID, FREQ freq) {
        return new PacketFrequencyItemGuiUpdate<>(hand, type, freq,
              new ArrayList<>(type.getManager(null).getFrequencies()),
              new ArrayList<>(type.getManager(ownerUUID).getFrequencies()));
    }

    public static <FREQ extends Frequency> void handle(PacketFrequencyItemGuiUpdate<FREQ> message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null && player.openContainer instanceof FrequencyItemContainer) {
                FrequencyItemContainer<FREQ> container = (FrequencyItemContainer<FREQ>) player.openContainer;
                container.handleCacheUpdate(message.publicCache, message.privateCache, message.frequency);
            }
        });
        ctx.setPacketHandled(true);
    }

    public static <FREQ extends Frequency> void encode(PacketFrequencyItemGuiUpdate<FREQ> pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.currentHand);
        pkt.frequencyType.write(buf);
        if (pkt.frequency == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            pkt.frequency.write(buf);
        }
        buf.writeVarInt(pkt.publicCache.size());
        for (FREQ freq : pkt.publicCache) {
            freq.write(buf);
        }
        buf.writeVarInt(pkt.privateCache.size());
        for (FREQ freq : pkt.privateCache) {
            freq.write(buf);
        }
    }

    public static <FREQ extends Frequency> PacketFrequencyItemGuiUpdate<FREQ> decode(PacketBuffer buf) {
        Hand currentHand = buf.readEnumValue(Hand.class);
        FrequencyType<FREQ> type = FrequencyType.load(buf);
        List<FREQ> publicCache = new ArrayList<>();
        List<FREQ> privateCache = new ArrayList<>();
        FREQ frequency = null;
        if (buf.readBoolean()) {
            frequency = Frequency.readFromPacket(buf);
        }
        int amount = buf.readVarInt();
        for (int i = 0; i < amount; i++) {
            publicCache.add(Frequency.readFromPacket(buf));
        }
        amount = buf.readVarInt();
        for (int i = 0; i < amount; i++) {
            privateCache.add(Frequency.readFromPacket(buf));
        }
        return new PacketFrequencyItemGuiUpdate<>(currentHand, type, frequency, publicCache, privateCache);
    }
}
