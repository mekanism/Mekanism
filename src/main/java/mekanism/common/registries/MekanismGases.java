package mekanism.common.registries;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.Fuel;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.Radiation;
import mekanism.common.ChemicalConstants;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.DeferredChemical.DeferredGas;
import mekanism.common.registration.impl.GasDeferredRegister;

public class MekanismGases {

    private MekanismGases() {
    }

    public static final GasDeferredRegister GASES = new GasDeferredRegister(Mekanism.MODID);

    public static final DeferredGas<Gas> HYDROGEN = GASES.register(ChemicalConstants.HYDROGEN, new Fuel(() -> 1, MekanismConfig.general.FROM_H2));
    public static final DeferredGas<Gas> OXYGEN = GASES.register(ChemicalConstants.OXYGEN);
    public static final DeferredGas<Gas> STEAM = GASES.register("steam", () -> new Gas(GasBuilder.builder(Mekanism.rl("liquid/steam"))));
    public static final DeferredGas<Gas> WATER_VAPOR = GASES.register("water_vapor", () -> new Gas(GasBuilder.builder(Mekanism.rl("liquid/steam"))));
    public static final DeferredGas<Gas> CHLORINE = GASES.register(ChemicalConstants.CHLORINE);
    public static final DeferredGas<Gas> SULFUR_DIOXIDE = GASES.register(ChemicalConstants.SULFUR_DIOXIDE);
    public static final DeferredGas<Gas> SULFUR_TRIOXIDE = GASES.register(ChemicalConstants.SULFUR_TRIOXIDE);
    public static final DeferredGas<Gas> SULFURIC_ACID = GASES.register(ChemicalConstants.SULFURIC_ACID);
    public static final DeferredGas<Gas> HYDROGEN_CHLORIDE = GASES.register(ChemicalConstants.HYDROGEN_CHLORIDE);
    public static final DeferredGas<Gas> HYDROFLUORIC_ACID = GASES.register(ChemicalConstants.HYDROFLUORIC_ACID);
    public static final DeferredGas<Gas> URANIUM_OXIDE = GASES.register(ChemicalConstants.URANIUM_OXIDE);
    public static final DeferredGas<Gas> URANIUM_HEXAFLUORIDE = GASES.register(ChemicalConstants.URANIUM_HEXAFLUORIDE);
    //Internal gases
    public static final DeferredGas<Gas> ETHENE = GASES.register(ChemicalConstants.ETHENE);
    public static final DeferredGas<Gas> SODIUM = GASES.register(ChemicalConstants.SODIUM, Coolants.SODIUM_COOLANT);
    public static final DeferredGas<Gas> SUPERHEATED_SODIUM = GASES.register(ChemicalConstants.SUPERHEATED_SODIUM, Coolants.HEATED_SODIUM_COOLANT);
    public static final DeferredGas<Gas> BRINE = GASES.register("brine", 0xFEEF9C);
    public static final DeferredGas<Gas> LITHIUM = GASES.register(ChemicalConstants.LITHIUM);
    public static final DeferredGas<Gas> OSMIUM = GASES.register("osmium", 0x52BDCA);
    public static final DeferredGas<Gas> FISSILE_FUEL = GASES.register("fissile_fuel", 0x2E332F);
    public static final DeferredGas<Gas> NUCLEAR_WASTE = GASES.register("nuclear_waste", 0x4F412A, new Radiation(0.01));
    public static final DeferredGas<Gas> SPENT_NUCLEAR_WASTE = GASES.register("spent_nuclear_waste", 0x262015, new Radiation(0.01));
    public static final DeferredGas<Gas> PLUTONIUM = GASES.register("plutonium", 0x1F919C, new Radiation(0.02));
    public static final DeferredGas<Gas> POLONIUM = GASES.register("polonium", 0x1B9E7B, new Radiation(0.05));
    public static final DeferredGas<Gas> ANTIMATTER = GASES.register("antimatter", 0xA464B3);

    @SuppressWarnings("Convert2MethodRef")
    public static class Coolants {

        private Coolants() {
        }

        // Do not change this to directly reference IGasProvider objects. This prevents a circular reference loop.
        public static final CooledCoolant SODIUM_COOLANT = new CooledCoolant(() -> SUPERHEATED_SODIUM.get(), 5, 1);
        public static final HeatedCoolant HEATED_SODIUM_COOLANT = new HeatedCoolant(() -> SODIUM.get(), 5, 1);
    }
}