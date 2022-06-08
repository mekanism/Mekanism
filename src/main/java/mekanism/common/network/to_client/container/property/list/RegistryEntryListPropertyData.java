package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;

public class RegistryEntryListPropertyData<V> extends ListPropertyData<V> {

    private final IForgeRegistry<V> registry;

    public RegistryEntryListPropertyData(short property, IForgeRegistry<V> registry, @Nonnull List<V> values) {
        super(property, ListType.REGISTRY_ENTRY, values);
        this.registry = registry;
    }

    static <V> RegistryEntryListPropertyData<V> read(short property, ListPropertyReader<V> reader) {
        //Unused registry just do null for now
        return new RegistryEntryListPropertyData<>(property, null, reader.apply(IForgeFriendlyByteBuf::readRegistryId));
    }

    @Override
    protected void writeListElement(FriendlyByteBuf buffer, V value) {
        buffer.writeRegistryId(registry, value);
    }
}