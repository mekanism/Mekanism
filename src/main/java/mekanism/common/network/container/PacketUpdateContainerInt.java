package mekanism.common.network.container;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} that does not truncate the `value` arg to a short
 */
public class PacketUpdateContainerInt extends PacketUpdateContainer<PacketUpdateContainerInt> {

    private final int value;

    public PacketUpdateContainerInt(short windowId, short property, int value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerInt(PacketBuffer buffer) {
        super(buffer);
        this.value = buffer.readVarInt();
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeVarInt(value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerInt message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerInt decode(PacketBuffer buf) {
        return new PacketUpdateContainerInt(buf);
    }
}