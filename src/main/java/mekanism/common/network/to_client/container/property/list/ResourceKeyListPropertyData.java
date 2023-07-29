package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public class ResourceKeyListPropertyData<V> extends ListPropertyData<ResourceKey<V>> {

    private final ResourceKey<? extends Registry<V>> registry;

    public ResourceKeyListPropertyData(short property, ResourceKey<? extends Registry<V>> registry, @NotNull List<ResourceKey<V>> values) {
        super(property, ListType.RESOURCE_KEY, values);
        this.registry = registry;
    }

    static <V> ResourceKeyListPropertyData<V> read(short property, FriendlyByteBuf buffer) {
        ResourceKey<? extends Registry<V>> registry = ResourceKey.createRegistryKey(buffer.readResourceLocation());
        return new ResourceKeyListPropertyData<>(property, registry, buffer.readList(r -> r.readResourceKey(registry)));
    }

    @Override
    protected void writeList(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(registry.location());
        super.writeList(buffer);
    }

    @Override
    protected void writeListElement(FriendlyByteBuf buffer, ResourceKey<V> value) {
        buffer.writeResourceKey(value);
    }
}