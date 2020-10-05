package mekanism.generators.common.registries;

import mekanism.api.chemical.gas.Gas;
import mekanism.common.registration.impl.GasDeferredRegister;
import mekanism.common.registration.impl.GasRegistryObject;
import mekanism.generators.common.GeneratorsChemicalConstants;
import mekanism.generators.common.MekanismGenerators;

public class GeneratorsGases {

    private GeneratorsGases() {
    }

    public static final GasDeferredRegister GASES = new GasDeferredRegister(MekanismGenerators.MODID);

    public static final GasRegistryObject<Gas> DEUTERIUM = GASES.register(GeneratorsChemicalConstants.DEUTERIUM);
    public static final GasRegistryObject<Gas> TRITIUM = GASES.register("tritium", 0x64FF70);
    public static final GasRegistryObject<Gas> FUSION_FUEL = GASES.register("fusion_fuel", 0x7E007D);
}