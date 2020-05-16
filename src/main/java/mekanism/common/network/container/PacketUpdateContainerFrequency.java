package mekanism.common.network.container;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.frequency.Frequency;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for frequencies
 */
public class PacketUpdateContainerFrequency<FREQUENCY extends Frequency> extends PacketUpdateContainer<PacketUpdateContainerFrequency<FREQUENCY>> {

    @Nullable
    private final FREQUENCY value;

    public PacketUpdateContainerFrequency(short windowId, short property, @Nullable FREQUENCY value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerFrequency(PacketBuffer buffer) {
        super(buffer);
        if (buffer.readBoolean()) {
            this.value = Frequency.readFromPacket(buffer);
        } else {
            this.value = null;
        }
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        if (value == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            value.write(buffer);
        }
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerFrequency<FREQUENCY> message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static <FREQUENCY extends Frequency> PacketUpdateContainerFrequency<FREQUENCY> decode(PacketBuffer buf) {
        return new PacketUpdateContainerFrequency<>(buf);
    }
}