package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistryEntryListPropertyData<V extends IForgeRegistryEntry<V>> extends ListPropertyData<V> {

    public RegistryEntryListPropertyData(short property, @Nonnull List<V> values) {
        super(property, ListType.REGISTRY_ENTRY, values);
    }

    static <V extends IForgeRegistryEntry<V>> RegistryEntryListPropertyData<V> read(short property, ListPropertyReader<V> reader) {
        return new RegistryEntryListPropertyData<>(property, reader.apply(IForgeFriendlyByteBuf::readRegistryId));
    }

    @Override
    protected void writeListElement(FriendlyByteBuf buffer, V value) {
        buffer.writeRegistryId(value);
    }
}