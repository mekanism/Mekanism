package mekanism.fabric_shim.registries;

import com.mojang.serialization.Lifecycle;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Builder for custom (modded) registries. Fabric-backed stand-in for NeoForge's RegistryBuilder
 * (same public surface, fresh implementation).
 *
 * <p>Like on NeoForge, {@link #create()} only constructs the registry; it becomes reachable through
 * the root registry once it is passed to {@link NewRegistryEvent#register(Registry)} and the shim
 * registry-event driver runs. Fabric-specific bookkeeping (registry sync) is applied at that point.
 */
public class RegistryBuilder<T> {

    /**
     * Registries built with {@code sync(true)}, consumed by {@link NewRegistryEvent} when the
     * registry is registered to the root registry.
     */
    static final Map<Registry<?>, Boolean> PENDING_SYNC = new IdentityHashMap<>();

    private final ResourceKey<? extends Registry<T>> registryKey;
    @Nullable
    private ResourceLocation defaultKey;
    private boolean intrusiveHolders = false;
    private boolean sync = false;

    public RegistryBuilder(ResourceKey<? extends Registry<T>> registryKey) {
        this.registryKey = registryKey;
    }

    public RegistryBuilder<T> defaultKey(ResourceLocation key) {
        this.defaultKey = key;
        return this;
    }

    public RegistryBuilder<T> defaultKey(ResourceKey<T> key) {
        this.defaultKey = key.location();
        return this;
    }

    public RegistryBuilder<T> withIntrusiveHolders() {
        this.intrusiveHolders = true;
        return this;
    }

    public RegistryBuilder<T> sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public RegistryBuilder<T> maxId(int maxId) {
        //Vanilla/Fabric registries have no id cap; accepted for source compatibility
        return this;
    }

    public Registry<T> create() {
        MappedRegistry<T> registry = this.defaultKey != null
              ? new DefaultedMappedRegistry<>(this.defaultKey.toString(), this.registryKey, Lifecycle.stable(), this.intrusiveHolders)
              : new MappedRegistry<>(this.registryKey, Lifecycle.stable(), this.intrusiveHolders);
        if (this.sync) {
            PENDING_SYNC.put(registry, Boolean.TRUE);
        }
        return registry;
    }
}
