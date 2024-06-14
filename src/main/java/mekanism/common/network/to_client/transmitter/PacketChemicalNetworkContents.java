package mekanism.common.network.to_client.transmitter;

import java.util.UUID;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.common.Mekanism;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketChemicalNetworkContents(UUID networkID, BoxedChemical chemical) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketChemicalNetworkContents> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("chemical_network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketChemicalNetworkContents> STREAM_CODEC = StreamCodec.composite(
          UUIDUtil.STREAM_CODEC, PacketChemicalNetworkContents::networkID,
          BoxedChemical.OPTIONAL_STREAM_CODEC, PacketChemicalNetworkContents::chemical,
          PacketChemicalNetworkContents::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketChemicalNetworkContents> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        //Note: We set the information even if opaque transmitters is true in case the client turns the config setting off
        // so that they will have the proper information to then render
        DynamicNetwork<?, ?, ?> clientNetwork = TransmitterNetworkRegistry.getInstance().getClientNetwork(networkID);
        if (clientNetwork instanceof BoxedChemicalNetwork network) {
            network.setLastChemical(chemical);
        }
    }
}