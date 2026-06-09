package mekanism.common.registration;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceKey;

public class DeferredMapCodecHolder<R, T extends R> extends MekanismDeferredHolder<MapCodec<? extends R>, MapCodec<T>> {

    protected DeferredMapCodecHolder(ResourceKey<MapCodec<? extends R>> key) {
        super(key);
    }
}