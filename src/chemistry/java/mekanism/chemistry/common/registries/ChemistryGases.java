package mekanism.chemistry.common.registries;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.common.registration.impl.GasDeferredRegister;

public class ChemistryGases {

    private ChemistryGases() {
    }

    public static final GasDeferredRegister GASES = new GasDeferredRegister(MekanismChemistry.MODID);
}
