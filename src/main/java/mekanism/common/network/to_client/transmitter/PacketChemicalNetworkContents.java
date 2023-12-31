package mekanism.common.network.to_client.transmitter;

import java.util.UUID;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.common.Mekanism;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketChemicalNetworkContents(UUID networkID, BoxedChemical chemical) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("chemical_network");

    public PacketChemicalNetworkContents(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), BoxedChemical.read(buffer));
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
        if (clientNetwork instanceof BoxedChemicalNetwork network) {
            network.setLastChemical(chemical);
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeUUID(networkID);
        chemical.write(buffer);
        PacketUtils.log("Sending type '{}' update message for chemical network with id {}", networkID);
    }
}