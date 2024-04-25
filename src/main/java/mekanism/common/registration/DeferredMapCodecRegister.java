package mekanism.common.registration;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DeferredMapCodecRegister<T> extends MekanismDeferredRegister<MapCodec<? extends T>> {

    public DeferredMapCodecRegister(ResourceKey<? extends Registry<MapCodec<? extends T>>> registryKey, String namespace) {
        this(registryKey, namespace, DeferredMapCodecHolder::new);
    }

    public DeferredMapCodecRegister(ResourceKey<? extends Registry<MapCodec<? extends T>>> registryKey, String namespace,
          Function<ResourceKey<MapCodec<? extends T>>, ? extends DeferredMapCodecHolder<T, ? extends T>> holderCreator) {
        super(registryKey, namespace, holderCreator);
    }

    public <I extends T> DeferredMapCodecHolder<T, I> registerCodec(String name, Function<ResourceLocation, MapCodec<I>> func) {
        return (DeferredMapCodecHolder<T, I>) super.register(name, func);
    }

    public <I extends T> DeferredMapCodecHolder<T, I> registerCodec(String name, Supplier<MapCodec<I>> sup) {
        return (DeferredMapCodecHolder<T, I>) register(name, sup);
    }
}