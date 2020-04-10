package mekanism.common.network.container;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for booleans
 */
public class PacketUpdateContainerBoolean extends PacketUpdateContainer<PacketUpdateContainerBoolean> {

    private final boolean value;

    public PacketUpdateContainerBoolean(short windowId, short property, boolean value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerBoolean(PacketBuffer buffer) {
        super(buffer);
        this.value = buffer.readBoolean();
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeBoolean(value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerBoolean message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerBoolean decode(PacketBuffer buf) {
        return new PacketUpdateContainerBoolean(buf);
    }
}