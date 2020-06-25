package mekanism.common.network.container.property.list;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.property.PropertyData;
import mekanism.common.network.container.property.PropertyType;
import net.minecraft.network.PacketBuffer;

public abstract class ListPropertyData<TYPE> extends PropertyData {

    @Nonnull
    protected final List<TYPE> values;
    private final ListType listType;

    public ListPropertyData(short property, ListType listType, @Nonnull List<TYPE> values) {
        super(PropertyType.LIST, property);
        this.listType = listType;
        this.values = values;
    }

    public static <TYPE> ListPropertyData<TYPE> readList(short property, PacketBuffer buffer) {
        ListType listType = buffer.readEnumValue(ListType.class);
        int elements = buffer.readVarInt();
        switch (listType) {
            case STRING:
                return (ListPropertyData<TYPE>) StringListPropertyData.read(property, elements, buffer);
            case FILTER:
                return (ListPropertyData<TYPE>) FilterListPropertyData.read(property, elements, buffer);
            case FREQUENCY:
                return (ListPropertyData<TYPE>) FrequencyListPropertyData.read(property, elements, buffer);
            default:
                Mekanism.logger.error("Unrecognized list type received: {}", listType);
                return null;
        }
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), values);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        buffer.writeEnumValue(listType);
        buffer.writeVarInt(values.size());
        writeListElements(buffer);
    }

    protected abstract void writeListElements(PacketBuffer buffer);
}