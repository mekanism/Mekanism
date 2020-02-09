package mekanism.common.network.container;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for doubles
 */
public class PacketUpdateContainerDouble extends PacketUpdateContainer<PacketUpdateContainerDouble> {

    private final double value;

    public PacketUpdateContainerDouble(short windowId, short property, double value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerDouble(PacketBuffer buffer) {
        super(buffer);
        this.value = buffer.readDouble();
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeDouble(value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerDouble message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerDouble decode(PacketBuffer buf) {
        return new PacketUpdateContainerDouble(buf);
    }
}