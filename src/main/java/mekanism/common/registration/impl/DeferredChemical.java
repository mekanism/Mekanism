package mekanism.common.registration.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.ISlurryProvider;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;

public class DeferredChemical<TYPE extends Chemical> extends MekanismDeferredHolder<Chemical, TYPE>
      implements IChemicalProvider {

    public DeferredChemical(ResourceKey<Chemical> key) {
        super(key);
    }

    @Override
    public Chemical getChemical() {
        return value();
    }

    public static class DeferredSlurry<SLURRY extends Chemical> extends DeferredChemical<SLURRY> implements ISlurryProvider {

        public DeferredSlurry(ResourceKey<Chemical> key) {
            super(key);
        }
    }
}