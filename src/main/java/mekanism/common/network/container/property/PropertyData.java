package mekanism.common.network.container.property;

import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.PacketUpdateContainer;
import mekanism.common.network.container.property.list.ListPropertyData;
import net.minecraft.network.PacketBuffer;

public abstract class PropertyData {

    private final PropertyType type;
    //TODO: Debate passing this over the network as an unsigned byte as this is value is bounded by
    // our max number of properties in a single container which I don't think will ever be super high
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

    public abstract PacketUpdateContainer<?> getSinglePacket(short windowId);

    public abstract void handleWindowProperty(MekanismContainer container);

    public void writeToPacket(PacketBuffer buffer) {
        buffer.writeEnumValue(type);
        buffer.writeShort(property);
    }

    public static PropertyData fromBuffer(PacketBuffer buffer) {
        PropertyType type = buffer.readEnumValue(PropertyType.class);
        short property = buffer.readShort();
        switch (type) {
            case BOOLEAN:
                return new BooleanPropertyData(property, buffer.readBoolean());
            case BYTE:
                return new BytePropertyData(property, buffer.readByte());
            case DOUBLE:
                return new DoublePropertyData(property, buffer.readDouble());
            case FLOAT:
                return new FloatPropertyData(property, buffer.readFloat());
            case INT:
                return new IntPropertyData(property, buffer.readVarInt());
            case LONG:
                return new LongPropertyData(property, buffer.readVarLong());
            case SHORT:
                return new ShortPropertyData(property, buffer.readShort());
            case ITEM_STACK:
                return new ItemStackPropertyData(property, buffer.readItemStack());
            case FLUID_STACK:
                return new FluidStackPropertyData(property, buffer.readFluidStack());
            case GAS_STACK:
                return new GasStackPropertyData(property, ChemicalUtils.readGasStack(buffer));
            case INFUSION_STACK:
                return new InfusionStackPropertyData(property, ChemicalUtils.readInfusionStack(buffer));
            case FREQUENCY:
                return FrequencyPropertyData.readFrequency(property, buffer);
            case FLOATING_LONG:
                return new FloatingLongPropertyData(property, FloatingLong.readFromBuffer(buffer));
            case LIST:
                return ListPropertyData.readList(property, buffer);
            default:
                Mekanism.logger.error("Unrecognized property type received: {}", type);
                return null;
        }
    }
}