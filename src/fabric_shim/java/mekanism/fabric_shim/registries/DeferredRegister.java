package mekanism.fabric_shim.registries;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects (name, factory) pairs and registers them when the shim registry events fire, handing out
 * {@link DeferredHolder}s up front.
 *
 * <p>Fabric-backed stand-in for NeoForge's DeferredRegister (same public surface, fresh
 * implementation). {@link #register(IEventBus)} subscribes to the shim {@link RegisterEvent} and
 * {@link NewRegistryEvent}, which {@link ShimRegistryEvents} posts from the Fabric mod initializer
 * in NeoForge's registration order — so entry construction order matches NeoForge exactly.
 */
public class DeferredRegister<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeferredRegister.class);

    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry.key(), namespace);
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String namespace) {
        return new DeferredRegister<>(key, namespace);
    }

    public static <B> DeferredRegister<B> create(ResourceLocation registryName, String modid) {
        return new DeferredRegister<>(ResourceKey.createRegistryKey(registryName), modid);
    }

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final Map<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Set<DeferredHolder<T, ? extends T>> entriesView = Collections.unmodifiableSet(entries.keySet());
    private final Map<ResourceLocation, ResourceLocation> aliases = new HashMap<>();

    @Nullable
    private Registry<T> customRegistry;
    @Nullable
    private RegistryHolder<T> registryHolder;
    private boolean seenRegisterEvent = false;
    private boolean seenNewRegistryEvent = false;
    private boolean registeredEventBus = false;

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = Objects.requireNonNull(registryKey);
        this.namespace = Objects.requireNonNull(namespace);
    }

    public <I extends T> DeferredHolder<T, I> register(final String name, final Supplier<? extends I> sup) {
        return this.register(name, key -> sup.get());
    }

    public <I extends T> DeferredHolder<T, I> register(final String name, final Function<ResourceLocation, ? extends I> func) {
        if (seenRegisterEvent) {
            throw new IllegalStateException("Cannot register new entries to DeferredRegister after RegisterEvent has been fired.");
        }
        Objects.requireNonNull(name);
        Objects.requireNonNull(func);
        final ResourceLocation key = ResourceLocation.fromNamespaceAndPath(namespace, name);
        DeferredHolder<T, I> ret = createHolder(this.registryKey, key);
        if (entries.putIfAbsent(ret, () -> func.apply(key)) != null) {
            throw new IllegalArgumentException("Duplicate registration " + name);
        }
        return ret;
    }

    protected <I extends T> DeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation key) {
        return DeferredHolder.create(registryKey, key);
    }

    /**
     * Configures a custom modded registry; the registry is created immediately and registered to the
     * root registry when {@link NewRegistryEvent} runs.
     */
    public Registry<T> makeRegistry(final Consumer<RegistryBuilder<T>> consumer) {
        if (BuiltInRegistries.REGISTRY.containsKey(this.registryKey.location()) || this.customRegistry != null) {
            throw new IllegalStateException("Cannot create a registry that already exists - " + this.registryKey);
        }
        if (this.seenNewRegistryEvent) {
            throw new IllegalStateException("Cannot create a registry after NewRegistryEvent was fired");
        }
        RegistryBuilder<T> registryBuilder = new RegistryBuilder<>(this.registryKey);
        consumer.accept(registryBuilder);
        this.customRegistry = registryBuilder.create();
        this.registryHolder = new RegistryHolder<>(this.registryKey);
        this.registryHolder.registry = this.customRegistry;
        return this.customRegistry;
    }

    public Supplier<Registry<T>> getRegistry() {
        if (this.registryHolder == null) {
            this.registryHolder = new RegistryHolder<>(this.registryKey);
        }
        return this.registryHolder;
    }

    public TagKey<T> createTagKey(String path) {
        Objects.requireNonNull(path);
        return createTagKey(ResourceLocation.fromNamespaceAndPath(this.namespace, path));
    }

    public TagKey<T> createTagKey(ResourceLocation location) {
        Objects.requireNonNull(location);
        return TagKey.create(this.registryKey, location);
    }

    /**
     * Registers a name alias. Vanilla/Fabric registries have no alias support, so for now aliases
     * are recorded but not applied.
     * TODO(fabric-port, Phase 3): honor aliases when decoding old ids (items, data components).
     */
    public void addAlias(ResourceLocation from, ResourceLocation to) {
        if (seenRegisterEvent) {
            throw new IllegalStateException("Cannot add aliases to DeferredRegister after RegisterEvent has been fired.");
        }
        this.aliases.put(from, to);
    }

    /**
     * Subscribes this register to the shim registry lifecycle. Must be called for entries to register.
     */
    public void register(IEventBus bus) {
        if (this.registeredEventBus) {
            throw new IllegalStateException("Cannot register DeferredRegister to more than one event bus.");
        }
        this.registeredEventBus = true;
        bus.addListener(this::addEntries);
        bus.addListener(this::addRegistry);
    }

    public Collection<DeferredHolder<T, ? extends T>> getEntries() {
        return entriesView;
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return this.registryKey;
    }

    public ResourceLocation getRegistryName() {
        return this.registryKey.location();
    }

    public String getNamespace() {
        return this.namespace;
    }

    private void addEntries(RegisterEvent event) {
        if (!event.getRegistryKey().equals(this.registryKey)) {
            return;
        }
        this.seenRegisterEvent = true;
        if (!this.aliases.isEmpty()) {
            LOGGER.warn("Registry aliases for {} are not yet applied on the Fabric port: {}", this.registryKey.location(), this.aliases);
        }
        for (Entry<DeferredHolder<T, ? extends T>, Supplier<? extends T>> e : entries.entrySet()) {
            event.register(this.registryKey, e.getKey().getId(), () -> e.getValue().get());
            e.getKey().bind(false);
        }
    }

    private void addRegistry(NewRegistryEvent event) {
        this.seenNewRegistryEvent = true;
        if (this.customRegistry != null) {
            event.register(this.customRegistry);
        }
    }

    private static class RegistryHolder<V> implements Supplier<Registry<V>> {

        private final ResourceKey<? extends Registry<V>> registryKey;
        private Registry<V> registry = null;

        private RegistryHolder(ResourceKey<? extends Registry<V>> registryKey) {
            this.registryKey = registryKey;
        }

        @SuppressWarnings("unchecked")
        @Override
        @Nullable
        public Registry<V> get() {
            //Keep looking up the registry until it is present
            if (this.registry == null) {
                this.registry = (Registry<V>) BuiltInRegistries.REGISTRY.get(this.registryKey.location());
            }
            return this.registry;
        }
    }
}
