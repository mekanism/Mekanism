package mekanism.common.registration;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class MekanismDeferredHolder<R, T extends R> extends DeferredHolder<R, T> implements INamedEntry {

    public MekanismDeferredHolder(ResourceKey<R> key) {
        super(key);
    }

    @Override
    public String getInternalRegistryName() {
        return getId().getPath();
    }
}