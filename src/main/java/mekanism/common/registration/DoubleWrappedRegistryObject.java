package mekanism.common.registration;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class DoubleWrappedRegistryObject<PRIMARY_REGISTRY, PRIMARY extends PRIMARY_REGISTRY, SECONDARY_REGISTRY, SECONDARY extends SECONDARY_REGISTRY>
      extends MekanismDeferredHolder<PRIMARY_REGISTRY, PRIMARY> {

    protected final DeferredHolder<SECONDARY_REGISTRY, SECONDARY> secondaryRO;

    public DoubleWrappedRegistryObject(DeferredHolder<PRIMARY_REGISTRY, PRIMARY> primaryKey, DeferredHolder<SECONDARY_REGISTRY, SECONDARY> secondaryRO) {
        this(primaryKey.getKey(), secondaryRO);
    }

    public DoubleWrappedRegistryObject(ResourceKey<PRIMARY_REGISTRY> primaryKey, DeferredHolder<SECONDARY_REGISTRY, SECONDARY> secondaryRO) {
        super(primaryKey);
        this.secondaryRO = secondaryRO;
    }

    public SECONDARY getSecondary() {
        return secondaryRO.get();
    }

    public boolean secondaryKeyMatches(Holder<SECONDARY_REGISTRY> holder) {
        return holder.is(secondaryRO.getKey());
    }

    public boolean isSecondary(SECONDARY_REGISTRY other) {
        return getSecondary() == other;
    }
}