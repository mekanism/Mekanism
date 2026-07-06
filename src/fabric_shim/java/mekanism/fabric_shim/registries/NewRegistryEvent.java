package mekanism.fabric_shim.registries;

import java.util.ArrayList;
import java.util.List;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.Event;

/**
 * Fired on the mod bus when custom registries can be registered. Stand-in for NeoForge's
 * NewRegistryEvent (same public surface, fresh implementation): collected registries are added to
 * the root registry after the event via Fabric's registry API, which also handles unfreezing and
 * sync bookkeeping.
 */
public class NewRegistryEvent extends Event implements IModBusEvent {

    private final List<Registry<?>> registries = new ArrayList<>();

    NewRegistryEvent() {
    }

    /**
     * Creates a registry from the {@code builder} and registers it.
     */
    public <T> Registry<T> create(RegistryBuilder<T> builder) {
        Registry<T> registry = builder.create();
        register(registry);
        return registry;
    }

    /**
     * Registers an already-created registry, allowing registries to live in static final fields.
     */
    public <T> void register(Registry<T> registry) {
        this.registries.add(registry);
    }

    void fill() {
        for (Registry<?> registry : this.registries) {
            registerToRootRegistry(registry);
        }
    }

    private static <T> void registerToRootRegistry(Registry<T> registry) {
        if (BuiltInRegistries.REGISTRY.containsKey(registry.key().location())) {
            throw new IllegalStateException("Attempted duplicate registration of registry " + registry.key().location());
        }
        FabricRegistryBuilder<T, WritableRegistry<T>> fabricBuilder = FabricRegistryBuilder.from((WritableRegistry<T>) registry);
        if (RegistryBuilder.PENDING_SYNC.remove(registry) != null) {
            fabricBuilder.attribute(RegistryAttribute.SYNCED);
        }
        fabricBuilder.buildAndRegister();
    }
}
