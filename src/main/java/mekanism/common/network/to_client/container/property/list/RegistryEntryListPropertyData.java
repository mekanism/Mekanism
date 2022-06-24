package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;

public class RegistryEntryListPropertyData<V> extends ListPropertyData<V> {

    private final IForgeRegistry<V> registry;

    public RegistryEntryListPropertyData(short property, IForgeRegistry<V> registry, @NotNull List<V> values) {
        super(property, ListType.REGISTRY_ENTRY, values);
        this.registry = registry;
    }

    static <V> RegistryEntryListPropertyData<V> read(short property, FriendlyByteBuf buffer) {
        //Based off of IForgeFriendlyByteBuf#readRegistryId but split into two parts so we only have to write it once for the entire list
        //TODO: If forge ever actually changes the registry name to being an id update this
        IForgeRegistry<V> registry = RegistryManager.ACTIVE.getRegistry(buffer.readResourceLocation());
        return new RegistryEntryListPropertyData<>(property, registry, buffer.readList(r -> r.readRegistryIdUnsafe(registry)));
    }

    @Override
    protected void writeList(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(registry.getRegistryName());
        super.writeList(buffer);
    }

    @Override
    protected void writeListElement(FriendlyByteBuf buffer, V value) {
        buffer.writeRegistryIdUnsafe(registry, value);
    }
}