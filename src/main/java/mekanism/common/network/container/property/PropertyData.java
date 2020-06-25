package mekanism.common.network.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.PacketUpdateContainer;
import net.minecraft.network.PacketBuffer;

public abstract class PropertyData {

    private final PropertyType type;
    private final short property;

    protected PropertyData(PropertyType type, short property) {
        this.type = type;
        this.property = property;
    }

    public PropertyType getType() {
        return type;
    }

    public short getProperty() {
        return property;
    }

    public PacketUpdateContainer getSinglePacket(short windowId) {
        return new PacketUpdateContainer(windowId, property, this);
    }

    public abstract void handleWindowProperty(MekanismContainer container);

    public void writeToPacket(PacketBuffer buffer) {
        buffer.writeEnumValue(type);
        buffer.writeShort(property);
    }

    public static PropertyData fromBuffer(PacketBuffer buffer) {
        PropertyType type = buffer.readEnumValue(PropertyType.class);
        short property = buffer.readShort();
        return type.createData(property, buffer);
    }
}