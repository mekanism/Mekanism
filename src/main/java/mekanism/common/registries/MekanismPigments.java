package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.PigmentDeferredRegister;

public class MekanismPigments {

    private MekanismPigments() {
    }

    public static final PigmentDeferredRegister PIGMENTS = new PigmentDeferredRegister(Mekanism.MODID);
}