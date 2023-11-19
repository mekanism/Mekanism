package mekanism.common.registration;

import com.mojang.serialization.Codec;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.resources.ResourceKey;

@NothingNullByDefault
public class DeferredCodecHolder<R, T extends R> extends MekanismDeferredHolder<Codec<? extends R>, Codec<T>> {

    protected DeferredCodecHolder(ResourceKey<Codec<? extends R>> key) {
        super(key);
    }
}