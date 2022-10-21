package mekanism.common.registration.impl;

import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class SlurryRegistryObject<DIRTY extends Slurry, CLEAN extends Slurry> extends DoubleWrappedRegistryObject<DIRTY, CLEAN> {

    public SlurryRegistryObject(RegistryObject<DIRTY> dirtyRO, RegistryObject<CLEAN> cleanRO) {
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