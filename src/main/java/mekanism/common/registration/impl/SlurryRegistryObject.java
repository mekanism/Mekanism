package mekanism.common.registration.impl;

import mekanism.api.chemical.Chemical;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import org.jetbrains.annotations.NotNull;

public class SlurryRegistryObject<DIRTY extends Chemical, CLEAN extends Chemical> extends DoubleWrappedRegistryObject<Chemical, DIRTY, Chemical, CLEAN> {

    public SlurryRegistryObject(DeferredChemical<DIRTY> dirtyRO, DeferredChemical<CLEAN> cleanRO) {
        super(dirtyRO, cleanRO);
    }

    @NotNull
    public DIRTY getDirtySlurry() {
        return getPrimary();
    }

    @NotNull
    public CLEAN getCleanSlurry() {
        return getSecondary();
    }
}