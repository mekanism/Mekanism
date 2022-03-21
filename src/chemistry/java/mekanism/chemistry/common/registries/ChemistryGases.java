package mekanism.chemistry.common.registries;

import mekanism.api.chemical.gas.Gas;
import mekanism.chemistry.common.ChemistryChemicalConstants;
import mekanism.chemistry.common.MekanismChemistry;
import mekanism.common.registration.impl.GasDeferredRegister;
import mekanism.common.registration.impl.GasRegistryObject;

public class ChemistryGases {

    private ChemistryGases() {
    }

    public static final GasDeferredRegister GASES = new GasDeferredRegister(MekanismChemistry.MODID);

    public static final GasRegistryObject<Gas> AMMONIA = GASES.register(ChemistryChemicalConstants.AMMONIA);
}
