package mekanism.common.network.to_server.frequency;

import java.util.UUID;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketSetFrequencyColor(boolean next, TypedIdentity data) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketSetFrequencyColor> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("set_frequency_color"));
    public static final StreamCodec<FriendlyByteBuf, PacketSetFrequencyColor> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.BOOL, PacketSetFrequencyColor::next,
          TypedIdentity.STREAM_CODEC, PacketSetFrequencyColor::data,
          PacketSetFrequencyColor::new
    );

    public <FREQ extends Frequency & IColorableFrequency> PacketSetFrequencyColor(FREQ freq, boolean next) {
        this(next, new TypedIdentity(freq.getType(), freq.getIdentity()));
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSetFrequencyColor> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        UUID player = context.player().getUUID();
        Frequency freq = data.type().getFrequency(data.data(), player);
        if (freq instanceof IColorableFrequency colorableFrequency && freq.ownerMatches(player)) {//Only allow changing the color if the owner of the frequency
            EnumColor color = colorableFrequency.getColor();
            colorableFrequency.setColor(next ? color.getNext() : color.getPrevious());
        }
    }
}