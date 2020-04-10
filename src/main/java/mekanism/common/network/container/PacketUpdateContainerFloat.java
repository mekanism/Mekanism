package mekanism.common.network.container;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for floats
 */
public class PacketUpdateContainerFloat extends PacketUpdateContainer<PacketUpdateContainerFloat> {

    private final float value;

    public PacketUpdateContainerFloat(short windowId, short property, float value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerFloat(PacketBuffer buffer) {
        super(buffer);
        this.value = buffer.readFloat();
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeFloat(value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerFloat message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerFloat decode(PacketBuffer buf) {
        return new PacketUpdateContainerFloat(buf);
    }
}