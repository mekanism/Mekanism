package mekanism.common.registration.impl;

import mekanism.api.chemical.Chemical;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class SlurryRegistryObject<DIRTY extends Chemical, CLEAN extends Chemical> extends DoubleWrappedRegistryObject<Chemical, DIRTY, Chemical, CLEAN> {

    public SlurryRegistryObject(DeferredChemical<DIRTY> dirtyRO, DeferredChemical<CLEAN> cleanRO) {
        super(dirtyRO, cleanRO);
    }

    @NotNull
    public DeferredHolder<Chemical, CLEAN> getCleanSlurry() {
        return secondaryRO;
    }
}