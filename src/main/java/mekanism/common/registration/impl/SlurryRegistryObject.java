package mekanism.common.registration.impl;

import javax.annotation.Nonnull;

import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import mekanism.common.registration.TripleWrappedRegistryObject;
import net.minecraftforge.fml.RegistryObject;

public class SlurryRegistryObject<DIRTY extends Slurry, CLEAN extends Slurry, PURE extends Slurry> extends TripleWrappedRegistryObject<DIRTY, CLEAN, PURE>
{
    public SlurryRegistryObject(RegistryObject<DIRTY> dirtyRO, RegistryObject<CLEAN> cleanRO, RegistryObject<PURE> pureRO)
    {
        super(dirtyRO, cleanRO, pureRO);
    }

    @Nonnull
    public DIRTY getDirtySlurry()
    {
        return getPrimary();
    }

    @Nonnull
    public CLEAN getCleanSlurry()
    {
        return getSecondary();
    }

    @Nonnull
    public PURE getPureSlurry()
    {
        return getTertiary();
    }
}