package mekanism.common.registration;

import com.mojang.serialization.MapCodec;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.resources.ResourceKey;

@NothingNullByDefault
public class DeferredMapCodecHolder<R, T extends R> extends MekanismDeferredHolder<MapCodec<? extends R>, MapCodec<T>> {

    protected DeferredMapCodecHolder(ResourceKey<MapCodec<? extends R>> key) {
        super(key);
    }
}