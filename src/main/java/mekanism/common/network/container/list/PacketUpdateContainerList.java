package mekanism.common.network.container.list;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.PacketUpdateContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for lists
 */
public abstract class PacketUpdateContainerList<TYPE> extends PacketUpdateContainer<PacketUpdateContainerList<TYPE>> {

    @Nonnull
    protected final List<TYPE> values;

    public PacketUpdateContainerList(short windowId, short property, @Nonnull List<TYPE> values) {
        super(windowId, property);
        this.values = values;
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeVarInt(values.size());
        writeListElements(buffer);
    }

    protected abstract void writeListElements(PacketBuffer buffer);

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerList<TYPE> message) {
        container.handleWindowProperty(message.property, message.values);
    }
}