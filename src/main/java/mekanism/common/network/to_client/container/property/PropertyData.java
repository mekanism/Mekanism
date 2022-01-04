package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;

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

    public abstract void handleWindowProperty(MekanismContainer container);

    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
        buffer.writeShort(property);
    }

    public static PropertyData fromBuffer(FriendlyByteBuf buffer) {
        PropertyType type = buffer.readEnum(PropertyType.class);
        short property = buffer.readShort();
        return type.createData(property, buffer);
    }
}