package mekanism.common.registration;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class MekanismDeferredHolder<R, T extends R> extends DeferredHolder<R, T> implements INamedEntry {

    public MekanismDeferredHolder(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation valueName) {
        this(ResourceKey.create(registryKey, valueName));
    }

    public MekanismDeferredHolder(ResourceKey<R> key) {
        super(key);
    }

    @Override
    public String getName() {
        return INamedEntry.super.getName();
    }

    public boolean is(R other) {
        return get() == other;
    }

    public boolean keyMatches(Holder<R> holder) {
        return holder.is(getKey());
    }
}