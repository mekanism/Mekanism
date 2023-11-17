package mekanism.common.registration.impl;

import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class SlurryRegistryObject<DIRTY extends Slurry, CLEAN extends Slurry> extends DoubleWrappedRegistryObject<Slurry, DIRTY, Slurry, CLEAN> {

    public SlurryRegistryObject(DeferredHolder<Slurry, DIRTY> dirtyRO, DeferredHolder<Slurry, CLEAN> cleanRO) {
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