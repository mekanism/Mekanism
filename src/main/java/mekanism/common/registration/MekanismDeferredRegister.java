package mekanism.common.registration;

import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

@NothingNullByDefault
public class MekanismDeferredRegister<T> extends DeferredRegister<T> {

    private final Function<ResourceKey<T>, ? extends MekanismDeferredHolder<T, ?>> holderCreator;

    public MekanismDeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this(registryKey, namespace, MekanismDeferredHolder::new);
    }

    public MekanismDeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace,
          Function<ResourceKey<T>, ? extends MekanismDeferredHolder<T, ? extends T>> holderCreator) {
        super(registryKey, namespace);
        this.holderCreator = holderCreator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends T> MekanismDeferredHolder<T, I> register(String name, Function<ResourceLocation, ? extends I> func) {
        return (MekanismDeferredHolder<T, I>) super.register(name, func);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends T> MekanismDeferredHolder<T, I> register(String name, Supplier<? extends I> sup) {
        return (MekanismDeferredHolder<T, I>) super.register(name, sup);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <I extends T> MekanismDeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation key) {
        return (MekanismDeferredHolder<T, I>) holderCreator.apply(ResourceKey.create(registryKey, key));
    }
}