package mekanism.fabric_shim.registries;

import com.mojang.datafixers.util.Either;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Holder} constructed from only a {@link ResourceKey} that resolves lazily against its
 * registry once the target object has been registered.
 *
 * <p>Fabric-backed stand-in for NeoForge's DeferredHolder (same public surface, fresh
 * implementation). Works for vanilla registries and for custom registries created through
 * {@link RegistryBuilder}, since both are reachable through the root registry.
 *
 * @param <R> the registry type
 * @param <T> the concrete type of the referenced entry
 */
public class DeferredHolder<R, T extends R> implements Holder<R>, Supplier<T> {

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation valueName) {
        return create(ResourceKey.create(registryKey, valueName));
    }

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceLocation registryName, ResourceLocation valueName) {
        return create(ResourceKey.createRegistryKey(registryName), valueName);
    }

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
        return new DeferredHolder<>(key);
    }

    protected final ResourceKey<R> key;
    @Nullable
    private Holder<R> holder = null;

    protected DeferredHolder(ResourceKey<R> key) {
        this.key = Objects.requireNonNull(key);
        this.bind(false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T value() {
        bind(true);
        if (this.holder == null) {
            throw new NullPointerException("Trying to access unbound value: " + this.key);
        }
        return (T) this.holder.value();
    }

    @Override
    public T get() {
        return this.value();
    }

    public Optional<T> asOptional() {
        return isBound() ? Optional.of(value()) : Optional.empty();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected Registry<R> getRegistry() {
        return (Registry<R>) BuiltInRegistries.REGISTRY.get(this.key.registry());
    }

    /**
     * Resolves the underlying registry holder if the registry (and ideally the entry) is available.
     * Safe to call repeatedly; no-op once resolved.
     */
    protected final void bind(boolean throwOnMissingRegistry) {
        if (this.holder != null) {
            return;
        }
        Registry<R> registry = getRegistry();
        if (registry != null) {
            this.holder = registry.getHolder(this.key).orElse(null);
        } else if (throwOnMissingRegistry) {
            throw new IllegalStateException("Registry not present for " + this + ": " + this.key.registry());
        }
    }

    public ResourceLocation getId() {
        return this.key.location();
    }

    /**
     * Note: on NeoForge this overrides Holder#getKey(), which is a NeoForge patch to the vanilla
     * interface. Vanilla/Fabric Holder has no such method, so here it only exists on this type;
     * call sites holding a plain Holder must use unwrapKey() instead.
     */
    public ResourceKey<R> getKey() {
        return this.key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        //ResourceKeys are interned, so identity comparison against our key is safe
        return obj instanceof Holder<?> h && h.kind() == Kind.REFERENCE && h.unwrapKey().orElse(null) == this.key;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "DeferredHolder{%s}", this.key);
    }

    @Override
    public boolean isBound() {
        bind(false);
        return this.holder != null && this.holder.isBound();
    }

    @Override
    public boolean is(ResourceLocation id) {
        return id.equals(this.key.location());
    }

    @Override
    public boolean is(ResourceKey<R> key) {
        return key == this.key;
    }

    @Override
    public boolean is(Predicate<ResourceKey<R>> filter) {
        return filter.test(this.key);
    }

    @Override
    public boolean is(TagKey<R> tag) {
        bind(false);
        return this.holder != null && this.holder.is(tag);
    }

    @Override
    public boolean is(Holder<R> holder) {
        bind(false);
        return this.holder != null && this.holder.is(holder);
    }

    @Override
    public Stream<TagKey<R>> tags() {
        bind(false);
        return this.holder != null ? this.holder.tags() : Stream.empty();
    }

    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        //Holder.Reference always returns the key; mirror that here
        return Either.left(this.key);
    }

    @Override
    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<R> owner) {
        bind(false);
        return this.holder != null && this.holder.canSerializeIn(owner);
    }
}
