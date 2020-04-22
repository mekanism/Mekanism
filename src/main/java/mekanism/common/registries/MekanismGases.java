package mekanism.common.registries;

import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.Slurry;
import mekanism.api.chemical.gas.attribute.GasAttributes.Radiation;
import mekanism.common.ChemicalConstants;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.GasDeferredRegister;
import mekanism.common.registration.impl.GasRegistryObject;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.SecondaryResource;

public class MekanismGases {

    public static final GasDeferredRegister GASES = new GasDeferredRegister(Mekanism.MODID);

    public static final Map<PrimaryResource, SlurryRegistryObject<Slurry, Slurry>> PROCESSED_RESOURCE_SLURRIES = new LinkedHashMap<>();

    public static final GasRegistryObject<Gas> HYDROGEN = GASES.register(ChemicalConstants.HYDROGEN);
    public static final GasRegistryObject<Gas> OXYGEN = GASES.register(ChemicalConstants.OXYGEN);
    public static final GasRegistryObject<Gas> STEAM = GASES.register("steam", () -> new Gas(GasBuilder.builder(Mekanism.rl("liquid/steam"))));
    public static final GasRegistryObject<Gas> WATER_VAPOR = GASES.register("water_vapor", () -> new Gas(GasBuilder.builder(Mekanism.rl("liquid/steam"))));
    public static final GasRegistryObject<Gas> CHLORINE = GASES.register(ChemicalConstants.CHLORINE);
    public static final GasRegistryObject<Gas> SULFUR_DIOXIDE = GASES.register(ChemicalConstants.SULFUR_DIOXIDE);
    public static final GasRegistryObject<Gas> SULFUR_TRIOXIDE = GASES.register(ChemicalConstants.SULFUR_TRIOXIDE);
    public static final GasRegistryObject<Gas> SULFURIC_ACID = GASES.register(ChemicalConstants.SULFURIC_ACID);
    public static final GasRegistryObject<Gas> HYDROGEN_CHLORIDE = GASES.register(ChemicalConstants.HYDROGEN_CHLORIDE);
    public static final GasRegistryObject<Gas> HYDROFLUORIC_ACID = GASES.register(ChemicalConstants.HYDROFLUORIC_ACID);
    public static final GasRegistryObject<Gas> URANIUM_OXIDE = GASES.register(ChemicalConstants.URANIUM_OXIDE);
    public static final GasRegistryObject<Gas> URANIUM_HEXAFLUORIDE = GASES.register(ChemicalConstants.URANIUM_HEXAFLUORIDE);
    //Internal gases
    public static final GasRegistryObject<Gas> ETHENE = GASES.register(ChemicalConstants.ETHENE);
    public static final GasRegistryObject<Gas> SODIUM = GASES.register(ChemicalConstants.SODIUM);
    public static final GasRegistryObject<Gas> BRINE = GASES.register("brine", 0xFEEF9C);
    public static final GasRegistryObject<Gas> LITHIUM = GASES.register(ChemicalConstants.LITHIUM);
    public static final GasRegistryObject<Gas> LIQUID_OSMIUM = GASES.register("liquid_osmium", 0x52BDCA);
    public static final GasRegistryObject<Gas> FISSILE_FUEL = GASES.register("fissile_fuel", 0x2E332F);
    public static final GasRegistryObject<Gas> NUCLEAR_WASTE = GASES.register("nuclear_waste", 0x4F412A, new Radiation(0.01));
    public static final GasRegistryObject<Gas> SPENT_NUCLEAR_WASTE = GASES.register("spent_nuclear_waste", 0x262015, new Radiation(0.01));
    public static final GasRegistryObject<Gas> PLUTONIUM = GASES.register("plutonium", 0x1F919C, new Radiation(0.02));
    public static final GasRegistryObject<Gas> POLONIUM = GASES.register("polonium", 0x1B9E7B, new Radiation(0.05));
    public static final GasRegistryObject<Gas> ANTIMATTER = GASES.register("antimatter", 0x7A91A1);
    public static final GasRegistryObject<Gas> NUTRITIONAL_PASTE = GASES.register("nutritional_paste", 0XEB6CA3);

    public static final SlurryRegistryObject<Slurry, Slurry> URANIUM_SLURRY = GASES.registerSlurry(SecondaryResource.URANIUM);

    static {
        for (PrimaryResource resource : PrimaryResource.values()) {
            PROCESSED_RESOURCE_SLURRIES.put(resource, GASES.registerSlurry(resource));
        }
    }
}