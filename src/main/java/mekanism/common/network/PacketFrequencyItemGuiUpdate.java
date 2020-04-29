package mekanism.common.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.inventory.container.item.FrequencyItemContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketFrequencyItemGuiUpdate<FREQ extends Frequency> {

    private List<FREQ> publicCache;
    private List<FREQ> privateCache;
    private FrequencyType<FREQ> frequencyType;
    private FREQ frequency;
    private Hand currentHand;

    private PacketFrequencyItemGuiUpdate(Hand hand, FrequencyType<FREQ> type, FREQ freq, List<FREQ> publicFreqs, List<FREQ> privateFreqs) {
        currentHand = hand;
        frequencyType = type;
        frequency = freq;

        publicCache = publicFreqs;
        privateCache = privateFreqs;
    }

    public static <FREQ extends Frequency> PacketFrequencyItemGuiUpdate<FREQ> create(Hand hand, FrequencyType<FREQ> type, UUID ownerUUID, FREQ freq) {
        return new PacketFrequencyItemGuiUpdate<>(hand, type, freq,
              type.getManager(null).getFrequencies().stream().collect(Collectors.toList()),
              type.getManager(ownerUUID).getFrequencies().stream().collect(Collectors.toList()));
    }

    public static <FREQ extends Frequency> void handle(PacketFrequencyItemGuiUpdate<FREQ> message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (player.openContainer instanceof FrequencyItemContainer) {
                FrequencyItemContainer<FREQ> container = (FrequencyItemContainer<FREQ>) player.openContainer;
                container.handleCacheUpdate(message.publicCache, message.privateCache, message.frequency);
            }
        });
        context.get().setPacketHandled(true);
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
            frequency = type.create(buf);
        }
        int amount = buf.readVarInt();
        for (int i = 0; i < amount; i++) {
            publicCache.add(type.create(buf));
        }
        amount = buf.readVarInt();
        for (int i = 0; i < amount; i++) {
            privateCache.add(type.create(buf));
        }
        return new PacketFrequencyItemGuiUpdate<FREQ>(currentHand, type, frequency, publicCache, privateCache);
    }
}
