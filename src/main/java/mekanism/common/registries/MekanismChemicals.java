package mekanism.common.registries;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributes.CooledCoolant;
import mekanism.api.chemical.attribute.ChemicalAttributes.Fuel;
import mekanism.api.chemical.attribute.ChemicalAttributes.HeatedCoolant;
import mekanism.api.chemical.attribute.ChemicalAttributes.Radiation;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.ChemicalConstants;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.DeferredChemical;
import mekanism.common.registration.impl.ChemicalDeferredRegister;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.util.EnumUtils;

public class MekanismChemicals {

    private MekanismChemicals() {
    }

    public static final ChemicalDeferredRegister CHEMICALS = new ChemicalDeferredRegister(Mekanism.MODID);

    public static final DeferredChemical<Chemical> BIO = CHEMICALS.register("bio", Mekanism.rl("infuse_type/bio"), 0x5A4630);
    public static final DeferredChemical<Chemical> FUNGI = CHEMICALS.register("fungi", Mekanism.rl("infuse_type/fungi"), 0x74656A);
    public static final DeferredChemical<Chemical> TIN = CHEMICALS.registerInfuse("tin", 0xCCCCD9);
    public static final DeferredChemical<Chemical> GOLD = CHEMICALS.registerInfuse("gold", 0xF2CD67);
    public static final DeferredChemical<Chemical> REFINED_OBSIDIAN = CHEMICALS.registerInfuse("refined_obsidian", 0x7C00ED);
    public static final DeferredChemical<Chemical> DIAMOND = CHEMICALS.registerInfuse("diamond", 0x6CEDD8);
    public static final DeferredChemical<Chemical> REDSTONE = CHEMICALS.registerInfuse("redstone", 0xB30505);
    public static final DeferredChemical<Chemical> CARBON = CHEMICALS.registerInfuse("carbon", 0x2C2C2C);

    public static final DeferredChemical<Chemical> HYDROGEN = CHEMICALS.registerGas(ChemicalConstants.HYDROGEN, new Fuel(() -> 1, MekanismConfig.general.FROM_H2));
    public static final DeferredChemical<Chemical> OXYGEN = CHEMICALS.registerGas(ChemicalConstants.OXYGEN);
    public static final DeferredChemical<Chemical> STEAM = CHEMICALS.register("steam", () -> new Chemical(ChemicalBuilder.builder(Mekanism.rl("liquid/steam")).gaseous()));
    public static final DeferredChemical<Chemical> WATER_VAPOR = CHEMICALS.register("water_vapor", () -> new Chemical(ChemicalBuilder.builder(Mekanism.rl("liquid/steam")).gaseous()));
    public static final DeferredChemical<Chemical> CHLORINE = CHEMICALS.register(ChemicalConstants.CHLORINE);
    public static final DeferredChemical<Chemical> SULFUR_DIOXIDE = CHEMICALS.registerGas(ChemicalConstants.SULFUR_DIOXIDE);
    public static final DeferredChemical<Chemical> SULFUR_TRIOXIDE = CHEMICALS.registerGas(ChemicalConstants.SULFUR_TRIOXIDE);
    public static final DeferredChemical<Chemical> SULFURIC_ACID = CHEMICALS.register(ChemicalConstants.SULFURIC_ACID);
    public static final DeferredChemical<Chemical> HYDROGEN_CHLORIDE = CHEMICALS.registerGas(ChemicalConstants.HYDROGEN_CHLORIDE);
    public static final DeferredChemical<Chemical> HYDROFLUORIC_ACID = CHEMICALS.register(ChemicalConstants.HYDROFLUORIC_ACID);
    public static final DeferredChemical<Chemical> URANIUM_OXIDE = CHEMICALS.register(ChemicalConstants.URANIUM_OXIDE);
    public static final DeferredChemical<Chemical> URANIUM_HEXAFLUORIDE = CHEMICALS.register(ChemicalConstants.URANIUM_HEXAFLUORIDE);
    //Internal gases
    public static final DeferredChemical<Chemical> ETHENE = CHEMICALS.registerGas(ChemicalConstants.ETHENE);
    public static final DeferredChemical<Chemical> SODIUM = CHEMICALS.register(ChemicalConstants.SODIUM, Coolants.SODIUM_COOLANT);
    public static final DeferredChemical<Chemical> SUPERHEATED_SODIUM = CHEMICALS.register(ChemicalConstants.SUPERHEATED_SODIUM, Coolants.HEATED_SODIUM_COOLANT);
    public static final DeferredChemical<Chemical> BRINE = CHEMICALS.registerGas("brine", 0xFEEF9C);
    public static final DeferredChemical<Chemical> LITHIUM = CHEMICALS.register(ChemicalConstants.LITHIUM);
    public static final DeferredChemical<Chemical> OSMIUM = CHEMICALS.register("osmium", 0x52BDCA);
    public static final DeferredChemical<Chemical> FISSILE_FUEL = CHEMICALS.register("fissile_fuel", 0x2E332F);
    public static final DeferredChemical<Chemical> NUCLEAR_WASTE = CHEMICALS.register("nuclear_waste", 0x4F412A, new Radiation(0.01));
    public static final DeferredChemical<Chemical> SPENT_NUCLEAR_WASTE = CHEMICALS.register("spent_nuclear_waste", 0x262015, new Radiation(0.01));
    public static final DeferredChemical<Chemical> PLUTONIUM = CHEMICALS.register("plutonium", 0x1F919C, new Radiation(0.02));
    public static final DeferredChemical<Chemical> POLONIUM = CHEMICALS.register("polonium", 0x1B9E7B, new Radiation(0.05));
    public static final DeferredChemical<Chemical> ANTIMATTER = CHEMICALS.register("antimatter", 0xA464B3);

    public static Map<EnumColor, IChemicalProvider> PIGMENT_COLOR_LOOKUP = new EnumMap<>(EnumColor.class);
    public static final Map<PrimaryResource, SlurryRegistryObject<Chemical, Chemical>> PROCESSED_RESOURCES = new LinkedHashMap<>();

    static {
        for (EnumColor color : EnumUtils.COLORS) {
            PIGMENT_COLOR_LOOKUP.put(color, registerPigment(color));
        }
        for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            PROCESSED_RESOURCES.put(resource, CHEMICALS.registerSlurry(resource));
        }
    }

    private static DeferredChemical<Chemical> registerPigment(EnumColor color) {
        int[] rgb = color.getRgbCode();
        int tint = rgb[0] << 16;
        tint |= rgb[1] << 8;
        tint |= rgb[2];
        return CHEMICALS.registerPigment(color.getRegistryPrefix(), tint);
    }

    @SuppressWarnings("Convert2MethodRef")
    public static class Coolants {

        private Coolants() {
        }

        // Do not change this to directly reference IChemicalProvider objects. This prevents a circular reference loop.
        public static final CooledCoolant SODIUM_COOLANT = new CooledCoolant(() -> SUPERHEATED_SODIUM.get(), 5, 1);
        public static final HeatedCoolant HEATED_SODIUM_COOLANT = new HeatedCoolant(() -> SODIUM.get(), 5, 1);
    }
}