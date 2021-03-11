package mekanism.common.network.to_client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mekanism.common.inventory.container.item.FrequencyItemContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketFrequencyItemGuiUpdate<FREQ extends Frequency> implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof FrequencyItemContainer) {
            FrequencyItemContainer<FREQ> container = (FrequencyItemContainer<FREQ>) player.containerMenu;
            container.handleCacheUpdate(publicCache, privateCache, frequency);
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(currentHand);
        frequencyType.write(buffer);
        if (frequency == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            frequency.write(buffer);
        }
        buffer.writeVarInt(publicCache.size());
        for (FREQ freq : publicCache) {
            freq.write(buffer);
        }
        buffer.writeVarInt(privateCache.size());
        for (FREQ freq : privateCache) {
            freq.write(buffer);
        }
    }

    public static <FREQ extends Frequency> PacketFrequencyItemGuiUpdate<FREQ> decode(PacketBuffer buffer) {
        Hand currentHand = buffer.readEnum(Hand.class);
        FrequencyType<FREQ> type = FrequencyType.load(buffer);
        List<FREQ> publicCache = new ArrayList<>();
        List<FREQ> privateCache = new ArrayList<>();
        FREQ frequency = null;
        if (buffer.readBoolean()) {
            frequency = Frequency.readFromPacket(buffer);
        }
        int amount = buffer.readVarInt();
        for (int i = 0; i < amount; i++) {
            publicCache.add(Frequency.readFromPacket(buffer));
        }
        amount = buffer.readVarInt();
        for (int i = 0; i < amount; i++) {
            privateCache.add(Frequency.readFromPacket(buffer));
        }
        return new PacketFrequencyItemGuiUpdate<>(currentHand, type, frequency, publicCache, privateCache);
    }
}
