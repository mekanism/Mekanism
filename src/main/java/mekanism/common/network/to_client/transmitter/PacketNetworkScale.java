package mekanism.common.network.to_client.transmitter;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketNetworkScale(UUID networkID, float scale) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("network_scale");

    public PacketNetworkScale(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readFloat());
    }

    public PacketNetworkScale(DynamicBufferedNetwork<?, ?, ?, ?> network) {
        this(network.getUUID(), network.currentScale);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        //Note: We set the information even if opaque transmitters is true in case the client turns the config setting off
        // so that they will have the proper information to then render
        DynamicNetwork<?, ?, ?> clientNetwork = TransmitterNetworkRegistry.getInstance().getClientNetwork(networkID);
        if (clientNetwork instanceof DynamicBufferedNetwork<?, ?, ?, ?> network) {
            network.currentScale = scale;
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeUUID(networkID);
        buffer.writeFloat(scale);
        PacketUtils.log("Sending scale '{}' update message for network with id {}", networkID);
    }
}