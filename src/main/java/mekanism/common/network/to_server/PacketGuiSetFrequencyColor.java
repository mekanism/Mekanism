package mekanism.common.network.to_server;

import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketGuiSetFrequencyColor<FREQ extends Frequency & IColorableFrequency> implements IMekanismPacket {

    private final FrequencyType<FREQ> frequencyType;
    private final FrequencyIdentity identity;
    private final boolean next;

    private PacketGuiSetFrequencyColor(FrequencyType<FREQ> frequencyType, FrequencyIdentity identity, boolean next) {
        this.frequencyType = frequencyType;
        this.identity = identity;
        this.next = next;
    }

    public static <FREQ extends Frequency & IColorableFrequency> PacketGuiSetFrequencyColor<FREQ> create(FREQ freq, boolean next) {
        return new PacketGuiSetFrequencyColor<>((FrequencyType<FREQ>) freq.getType(), freq.getIdentity(), next);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player != null) {
            FREQ freq = frequencyType.getFrequency(identity, player.getUUID());
            if (freq != null && freq.ownerMatches(player.getUUID())) {
                freq.setColor(next ? freq.getColor().getNext() : freq.getColor().getPrevious());
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        frequencyType.write(buffer);
        frequencyType.getIdentitySerializer().write(buffer, identity);
        buffer.writeBoolean(next);
    }

    public static <FREQ extends Frequency & IColorableFrequency> PacketGuiSetFrequencyColor<FREQ> decode(PacketBuffer buffer) {
        FrequencyType<FREQ> frequencyType = FrequencyType.load(buffer);
        FrequencyIdentity identity = frequencyType.getIdentitySerializer().read(buffer);
        return new PacketGuiSetFrequencyColor<>(frequencyType, identity, buffer.readBoolean());
    }
}