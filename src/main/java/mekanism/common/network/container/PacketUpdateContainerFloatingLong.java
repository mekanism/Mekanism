package mekanism.common.network.container;

import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for item stacks
 */
public class PacketUpdateContainerFloatingLong extends PacketUpdateContainer<PacketUpdateContainerFloatingLong> {

    @Nonnull
    private final FloatingLong value;

    public PacketUpdateContainerFloatingLong(short windowId, short property, @Nonnull FloatingLong value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerFloatingLong(PacketBuffer buffer) {
        super(buffer);
        this.value = FloatingLong.fromBuffer(buffer);
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        value.writeToBuffer(buffer);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerFloatingLong message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerFloatingLong decode(PacketBuffer buf) {
        return new PacketUpdateContainerFloatingLong(buf);
    }
}