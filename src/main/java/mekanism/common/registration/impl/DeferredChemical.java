package mekanism.common.registration.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IChemicalProvider;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DeferredChemical<TYPE extends Chemical> extends MekanismDeferredHolder<Chemical, TYPE> implements IChemicalProvider {

    public DeferredChemical(ResourceKey<Chemical> key) {
        super(key);
    }

    @NotNull
    @Override
    public Chemical getChemical() {
        return value();
    }

    @NotNull
    @Override
    public Holder<Chemical> getChemicalHolder() {
        return this;
    }

    @NotNull
    @Override
    public ResourceLocation getRegistryName() {
        return getId();
    }

    public boolean keyMatches(Holder<Chemical> holder) {
        return holder.is(getKey());
    }
}