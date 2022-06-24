package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;
import mekanism.common.network.to_client.container.property.list.RegistryEntryListPropertyData;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling registry entry based lists
 */
public class SyncableRegistryEntryList<V> extends SyncableList<V> {

    public static <V> SyncableRegistryEntryList<V> create(IForgeRegistry<V> registry, Supplier<@NotNull List<V>> getter, Consumer<@NotNull List<V>> setter) {
        return new SyncableRegistryEntryList<>(registry, getter, setter);
    }

    private final IForgeRegistry<V> registry;

    private SyncableRegistryEntryList(IForgeRegistry<V> registry, Supplier<@NotNull List<V>> getter, Consumer<@NotNull List<V>> setter) {
        super(getter, setter);
        this.registry = registry;
    }

    @Override
    public ListPropertyData<V> getPropertyData(short property, DirtyType dirtyType) {
        return new RegistryEntryListPropertyData<>(property, registry, get());
    }
}