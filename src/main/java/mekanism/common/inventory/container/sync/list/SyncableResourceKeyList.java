package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling registry entry based lists
 */
public class SyncableResourceKeyList<V> extends SyncableList<ResourceKey<V>> {

    public static <V> SyncableResourceKeyList<V> create(ResourceKey<? extends Registry<V>> registry, Supplier<@NotNull List<ResourceKey<V>>> getter,
          Consumer<@NotNull List<ResourceKey<V>>> setter) {
        return new SyncableResourceKeyList<>(registry, getter, setter);
    }

    private final ResourceKey<? extends Registry<V>> registry;

    private SyncableResourceKeyList(ResourceKey<? extends Registry<V>> registry, Supplier<@NotNull List<ResourceKey<V>>> getter,
          Consumer<@NotNull List<ResourceKey<V>>> setter) {
        super(getter, setter);
        this.registry = registry;
    }

    @Override
    protected List<ResourceKey<V>> deserializeList(RegistryFriendlyByteBuf buffer) {
        return buffer.readList(buf -> buf.readResourceKey(registry));
    }

    @Override
    protected void serializeListElement(RegistryFriendlyByteBuf buffer, ResourceKey<V> value) {
        buffer.writeResourceKey(value);
    }
}