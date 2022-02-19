package mekanism.common.network.to_client.container.property.list;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistryEntryListPropertyData<V extends IForgeRegistryEntry<V>> extends ListPropertyData<V> {

    public RegistryEntryListPropertyData(short property, @Nonnull List<V> values) {
        super(property, ListType.REGISTRY_ENTRY, values);
    }

    public static <V extends IForgeRegistryEntry<V>> RegistryEntryListPropertyData<V> read(short property, int elements, PacketBuffer buffer) {
        List<V> values = new ArrayList<>(elements);
        for (int i = 0; i < elements; i++) {
            values.add(buffer.readRegistryId());
        }
        return new RegistryEntryListPropertyData<>(property, values);
    }

    @Override
    protected void writeListElement(PacketBuffer buffer, V value) {
        buffer.writeRegistryId(value);
    }
}