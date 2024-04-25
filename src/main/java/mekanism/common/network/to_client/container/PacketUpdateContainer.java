package mekanism.common.network.to_client.container;

import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketUpdateContainer(short windowId, List<PropertyData> data) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketUpdateContainer> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("update_container"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateContainer> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PacketUpdateContainer::windowId,
          PropertyData.GENERIC_STREAM_CODEC.apply(ByteBufCodecs.list()), PacketUpdateContainer::data,
          PacketUpdateContainer::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketUpdateContainer> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        //Ensure that the container is one of ours, and the window id is the same as we expect it to be
        if (context.player().containerMenu instanceof MekanismContainer container && container.containerId == windowId) {
            // and if so handle the packet
            for (PropertyData datum : data) {
                datum.handleWindowProperty(container);
            }
        }
    }
}