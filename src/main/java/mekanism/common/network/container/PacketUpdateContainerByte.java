package mekanism.common.network.container;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for bytes
 */
public class PacketUpdateContainerByte extends PacketUpdateContainer<PacketUpdateContainerByte> {

    private final byte value;

    public PacketUpdateContainerByte(short windowId, short property, byte value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerByte(PacketBuffer buffer) {
        super(buffer);
        this.value = buffer.readByte();
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeByte(value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerByte message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerByte decode(PacketBuffer buf) {
        return new PacketUpdateContainerByte(buf);
    }
}