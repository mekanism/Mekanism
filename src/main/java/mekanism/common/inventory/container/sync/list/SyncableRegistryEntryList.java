package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.annotations.NonNull;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;
import mekanism.common.network.to_client.container.property.list.RegistryEntryListPropertyData;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling registry entry based lists
 */
public class SyncableRegistryEntryList<V extends IForgeRegistryEntry<V>> extends SyncableList<V> {

    public static <V extends IForgeRegistryEntry<V>> SyncableRegistryEntryList<V> create(Supplier<@NonNull List<V>> getter, Consumer<@NonNull List<V>> setter) {
        return new SyncableRegistryEntryList<>(getter, setter);
    }

    private SyncableRegistryEntryList(Supplier<@NonNull List<V>> getter, Consumer<@NonNull List<V>> setter) {
        super(getter, setter);
    }

    @Override
    public ListPropertyData<V> getPropertyData(short property, DirtyType dirtyType) {
        return new RegistryEntryListPropertyData<>(property, get());
    }
}