package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.network.to_client.container.property.RegistryEntryPropertyData;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling registry entries.
 */
public class SyncableRegistryEntry<V> implements ISyncableData {

    public static <V> SyncableRegistryEntry<V> create(IForgeRegistry<V> registry, Supplier<@NotNull V> getter, Consumer<@NotNull V> setter) {
        return new SyncableRegistryEntry<>(registry, getter, setter);
    }

    private final Supplier<@NotNull V> getter;
    private final Consumer<@NotNull V> setter;
    private final IForgeRegistry<V> registry;
    private V lastKnownValue;

    private SyncableRegistryEntry(IForgeRegistry<V> registry, Supplier<@NotNull V> getter, Consumer<@NotNull V> setter) {
        this.registry = registry;
        this.getter = getter;
        this.setter = setter;
    }

    @NotNull
    public V get() {
        return getter.get();
    }

    public void set(@NotNull V value) {
        setter.accept(value);
    }

    @Override
    public DirtyType isDirty() {
        V oldValue = get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return DirtyType.get(dirty);
    }

    @Override
    public RegistryEntryPropertyData<?> getPropertyData(short property, DirtyType dirtyType) {
        return new RegistryEntryPropertyData<>(property, registry, get());
    }
}