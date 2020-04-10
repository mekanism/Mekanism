package mekanism.common.network.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.PacketUpdateContainerLong;
import net.minecraft.network.PacketBuffer;

public class LongPropertyData extends PropertyData {

    private final long value;

    public LongPropertyData(short property, long value) {
        super(PropertyType.LONG, property);
        this.value = value;
    }

    @Override
    public PacketUpdateContainerLong getSinglePacket(short windowId) {
        return new PacketUpdateContainerLong(windowId, getProperty(), value);
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        buffer.writeVarLong(value);
    }
}