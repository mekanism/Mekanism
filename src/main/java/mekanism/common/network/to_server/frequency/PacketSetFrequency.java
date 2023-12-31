package mekanism.common.network.to_server.frequency;

import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public abstract class PacketSetFrequency<FREQ extends Frequency> implements IMekanismPacket<PlayPayloadContext> {

    protected final FrequencyType<FREQ> type;
    protected final FrequencyIdentity data;
    protected final boolean set;

    protected PacketSetFrequency(boolean set, FrequencyType<FREQ> type, FrequencyIdentity data) {
        this.type = type;
        this.data = data;
        this.set = set;
    }

    protected PacketSetFrequency(FriendlyByteBuf buf) {
        this.set = buf.readBoolean();
        this.type = FrequencyType.load(buf);
        this.data = this.type.getIdentitySerializer().read(buf);
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBoolean(set);
        type.write(buffer);
        type.getIdentitySerializer().write(buffer, data);
    }
}