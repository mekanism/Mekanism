package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import net.minecraftforge.fml.RegistryObject;

public class SlurryRegistryObject<DIRTY extends Slurry, CLEAN extends Slurry> extends DoubleWrappedRegistryObject<DIRTY, CLEAN> {

    public SlurryRegistryObject(RegistryObject<DIRTY> dirtyRO, RegistryObject<CLEAN> cleanRO) {
        super(dirtyRO, cleanRO);
    }

    @Nonnull
    public DIRTY getDirtySlurry() {
        return getPrimary();
    }

    @Nonnull
    public CLEAN getCleanSlurry() {
        return getSecondary();
    }
}