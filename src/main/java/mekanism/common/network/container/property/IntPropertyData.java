package mekanism.common.network.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

public class IntPropertyData extends PropertyData {

    private final int value;

    public IntPropertyData(short property, int value) {
        super(PropertyType.INT, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        buffer.writeVarInt(value);
    }
}