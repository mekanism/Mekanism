package mekanism.common.network.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

public class FloatPropertyData extends PropertyData {

    private final float value;

    public FloatPropertyData(short property, float value) {
        super(PropertyType.FLOAT, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        buffer.writeFloat(value);
    }
}