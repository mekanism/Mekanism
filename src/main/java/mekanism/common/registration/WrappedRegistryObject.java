package mekanism.common.registration;

import java.util.Objects;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class WrappedRegistryObject<R, T extends R> implements Supplier<T>, INamedEntry {

    protected DeferredHolder<R, T> registryObject;

    protected WrappedRegistryObject(DeferredHolder<R, T> registryObject) {
        this.registryObject = registryObject;
    }

    public Holder<R> holder() {
        return registryObject;
    }

    @Override
    public T get() {
        return registryObject.get();
    }

    @Override
    public String getInternalRegistryName() {
        return registryObject.getId().getPath();
    }

    public ResourceKey<R> key() {
        return Objects.requireNonNull(registryObject.getKey(), "Resource key should not be null");
    }
}