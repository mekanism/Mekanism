package mekanism.common.network.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

public class DoublePropertyData extends PropertyData {

    private final double value;

    public DoublePropertyData(short property, double value) {
        super(PropertyType.DOUBLE, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        buffer.writeDouble(value);
    }
}