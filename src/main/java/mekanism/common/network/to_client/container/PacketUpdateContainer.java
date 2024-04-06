package mekanism.common.network.to_client.container;

import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * @param windowId Note: gets transferred over the network as an unsigned byte
 */
public record PacketUpdateContainer(short windowId, List<PropertyData> data) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("update_container");

    public PacketUpdateContainer(FriendlyByteBuf buffer) {
        this(buffer.readUnsignedByte(), buffer.readList(PropertyData::fromBuffer));
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        MekanismContainer container = PacketUtils.container(context, MekanismContainer.class);
        //Ensure that the container is one of ours, and the window id is the same as we expect it to be
        if (container != null && container.containerId == windowId) {
            // and if so handle the packet
            for (PropertyData datum : data) {
                datum.handleWindowProperty(container);
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeByte(windowId);
        buffer.writeObjectCollection(data, PropertyData::writeToPacket);
    }
}