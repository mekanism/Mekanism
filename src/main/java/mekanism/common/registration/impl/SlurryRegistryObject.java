package mekanism.common.registration.impl;

import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import mekanism.common.registration.impl.DeferredChemical.DeferredSlurry;
import org.jetbrains.annotations.NotNull;

public class SlurryRegistryObject<DIRTY extends Slurry, CLEAN extends Slurry> extends DoubleWrappedRegistryObject<Slurry, DIRTY, Slurry, CLEAN> {

    public SlurryRegistryObject(DeferredSlurry<DIRTY> dirtyRO, DeferredSlurry<CLEAN> cleanRO) {
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