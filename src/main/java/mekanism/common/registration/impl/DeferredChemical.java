package mekanism.common.registration.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IChemicalProvider;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;
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
}