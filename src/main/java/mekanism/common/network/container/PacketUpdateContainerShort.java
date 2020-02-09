package mekanism.common.network.container;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for shorts
 */
public class PacketUpdateContainerShort extends PacketUpdateContainer<PacketUpdateContainerShort> {

    private final short value;

    public PacketUpdateContainerShort(short windowId, short property, short value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerShort(PacketBuffer buffer) {
        super(buffer);
        this.value = buffer.readShort();
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeShort(value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerShort message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerShort decode(PacketBuffer buf) {
        return new PacketUpdateContainerShort(buf);
    }
}