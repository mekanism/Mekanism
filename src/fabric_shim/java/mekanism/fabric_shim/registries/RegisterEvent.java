package mekanism.fabric_shim.registries;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Fired on the mod bus for each registry when it is ready to receive modded entries.
 * Stand-in for NeoForge's RegisterEvent (same public surface, fresh implementation);
 * posted by {@link ShimRegistryEvents} in NeoForge's registration order.
 */
public class RegisterEvent extends Event implements IModBusEvent {

    private final ResourceKey<? extends Registry<?>> registryKey;
    private final Registry<?> registry;

    RegisterEvent(ResourceKey<? extends Registry<?>> registryKey, Registry<?> registry) {
        this.registryKey = registryKey;
        this.registry = registry;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation name, Supplier<T> valueSupplier) {
        if (this.registryKey.equals(registryKey)) {
            Registry.register((Registry) this.registry, name, valueSupplier.get());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, Consumer<RegisterHelper<T>> consumer) {
        if (this.registryKey.equals(registryKey)) {
            consumer.accept((name, value) -> Registry.register((Registry) this.registry, name, value));
        }
    }

    public ResourceKey<? extends Registry<?>> getRegistryKey() {
        return this.registryKey;
    }

    public Registry<?> getRegistry() {
        return this.registry;
    }

    /**
     * @return the registry typed to the given key if it matches this event's registry, otherwise null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        //ResourceKeys are interned, so identity comparison is safe here (mirrors NeoForge)
        return key == this.registryKey ? (Registry<T>) this.registry : null;
    }

    @FunctionalInterface
    public interface RegisterHelper<T> {

        default void register(ResourceKey<T> key, T value) {
            register(key.location(), value);
        }

        void register(ResourceLocation name, T value);
    }
}
