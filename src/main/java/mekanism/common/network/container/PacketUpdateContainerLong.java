package mekanism.common.network.container;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for longs
 */
public class PacketUpdateContainerLong extends PacketUpdateContainer<PacketUpdateContainerLong> {

    private final long value;

    public PacketUpdateContainerLong(short windowId, short property, long value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerLong(PacketBuffer buffer) {
        super(buffer);
        this.value = buffer.readVarLong();
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeVarLong(value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerLong message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerLong decode(PacketBuffer buf) {
        return new PacketUpdateContainerLong(buf);
    }
}