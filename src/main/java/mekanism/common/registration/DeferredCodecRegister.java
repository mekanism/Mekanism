package mekanism.common.registration;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DeferredCodecRegister<T> extends MekanismDeferredRegister<Codec<? extends T>> {

    public DeferredCodecRegister(ResourceKey<? extends Registry<Codec<? extends T>>> registryKey, String namespace) {
        this(registryKey, namespace, DeferredCodecHolder::new);
    }

    public DeferredCodecRegister(ResourceKey<? extends Registry<Codec<? extends T>>> registryKey, String namespace,
          Function<ResourceKey<Codec<? extends T>>, ? extends DeferredCodecHolder<T, ? extends T>> holderCreator) {
        super(registryKey, namespace, holderCreator);
    }

    public <I extends T> DeferredCodecHolder<T, I> registerCodec(String name, Function<ResourceLocation, Codec<I>> func) {
        return (DeferredCodecHolder<T, I>) super.register(name, func);
    }

    public <I extends T> DeferredCodecHolder<T, I> registerCodec(String name, Supplier<Codec<I>> sup) {
        return (DeferredCodecHolder<T, I>) register(name, sup);
    }
}