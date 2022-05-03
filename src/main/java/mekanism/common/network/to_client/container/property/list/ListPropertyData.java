package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.FriendlyByteBuf;

public abstract class ListPropertyData<TYPE> extends PropertyData {

    @Nonnull
    protected final List<TYPE> values;
    private final ListType listType;

    public ListPropertyData(short property, ListType listType, @Nonnull List<TYPE> values) {
        super(PropertyType.LIST, property);
        this.listType = listType;
        this.values = values;
    }

    @SuppressWarnings("unchecked")
    public static <TYPE> ListPropertyData<TYPE> readList(short property, FriendlyByteBuf buffer) {
        return (ListPropertyData<TYPE>) switch (buffer.readEnum(ListType.class)) {
            case STRING -> StringListPropertyData.read(property, buffer::readList);
            case FILTER -> FilterListPropertyData.read(property, buffer::readList);
            case FREQUENCY -> FrequencyListPropertyData.read(property, buffer::readList);
            case REGISTRY_ENTRY -> RegistryEntryListPropertyData.read(property, buffer::readList);
        };
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), values);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        super.writeToPacket(buffer);
        buffer.writeEnum(listType);
        buffer.writeCollection(values, this::writeListElement);
    }

    protected abstract void writeListElement(FriendlyByteBuf buffer, TYPE value);

    interface ListPropertyReader<TYPE> extends Function<Function<FriendlyByteBuf, TYPE>, List<TYPE>> {
    }
}