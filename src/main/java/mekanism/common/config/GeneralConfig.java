package mekanism.common.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedConfigValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.config.value.CachedOredictionificatorConfigValue;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FluidTankTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class GeneralConfig extends BaseMekanismConfig {

    private static final String CONVERSION_CATEGORY = "energy_conversion";

    private static final String EJECT_CATEGORY = "auto_eject";
    private static final String MINER_CATEGORY = "digital_miner";
    private static final String DYNAMIC_TANK = "dynamic_tank";
    private static final String LASER_SETTINGS = "laser";
    private static final String OREDICTIONIFICATOR_CATEGORY = "oredictionificator";
    private static final String PUMP_CATEGORY = "pump";
    private static final String ENTANGLOPORTER_CATEGORY = "quantum_entangloporter";
    private static final String SECURITY_CATEGORY = "security";
    private static final String EVAPORATION_CATEGORY = "thermal_evaporation";
    private static final String SPS_CATEGORY = "sps";
    private static final String RADIATION_CATEGORY = "radiation";
    private static final String PREFILLED_CATEGORY = "prefilled";
    private static final String NUTRITIONAL_PASTE_CATEGORY = "nutritional_paste";

    private final ForgeConfigSpec configSpec;

    public final CachedBooleanValue logPackets;
    public final CachedBooleanValue allowChunkloading;
    public final CachedBooleanValue easyMinerFilters;
    public final CachedIntValue blockDeactivationDelay;
    public final CachedConfigValue<List<? extends String>> cardboardModBlacklist;
    public final CachedBooleanValue transmitterAlloyUpgrade;
    public final CachedIntValue maxUpgradeMultiplier;
    public final CachedDoubleValue boilerWaterConductivity;
    public final CachedDoubleValue heatPerFuelTick;
    public final CachedIntValue fuelwoodTickMultiplier;
    public final CachedDoubleValue resistiveHeaterEfficiency;
    public final CachedDoubleValue superheatingHeatTransfer;
    public final CachedIntValue maxSolarNeutronActivatorRate;
    //Auto eject
    public final CachedIntValue fluidAutoEjectRate;
    public final CachedLongValue chemicalAutoEjectRate;
    public final CachedDoubleValue dumpExcessKeepRatio;
    //Dynamic Tank
    public final CachedIntValue dynamicTankFluidPerTank;
    public final CachedLongValue dynamicTankChemicalPerTank;
    //Prefilled
    public final CachedBooleanValue prefilledFluidTanks;
    public final CachedBooleanValue prefilledGasTanks;
    public final CachedBooleanValue prefilledInfusionTanks;
    public final CachedBooleanValue prefilledPigmentTanks;
    public final CachedBooleanValue prefilledSlurryTanks;
    //Energy Conversion
    public final CachedBooleanValue blacklistIC2;
    public final CachedFloatingLongValue ic2ConversionRate;
    public final CachedBooleanValue blacklistForge;
    public final CachedFloatingLongValue forgeConversionRate;
    public final CachedBooleanValue blacklistFluxNetworks;
    public final CachedFloatingLongValue FROM_H2;
    public final CachedIntValue ETHENE_BURN_TIME;
    public final CachedFloatingLongValue maxEnergyPerSteam;
    //Radiation
    public final CachedBooleanValue radiationEnabled;
    public final CachedIntValue radiationChunkCheckRadius;
    public final CachedDoubleValue radiationSourceDecayRate;
    public final CachedDoubleValue radiationTargetDecayRate;
    public final CachedDoubleValue radiationNegativeEffectsMinSeverity;
    public final CachedLongValue radioactiveWasteBarrelMaxGas;
    public final CachedIntValue radioactiveWasteBarrelProcessTicks;
    public final CachedLongValue radioactiveWasteBarrelDecayAmount;
    //Digital Miner
    public final CachedIntValue minerSilkMultiplier;
    public final CachedIntValue minerMaxRadius;
    public final CachedIntValue minerTicksPerMine;
    //Laser
    public final CachedBooleanValue aestheticWorldDamage;
    public final CachedIntValue laserRange;
    public final CachedFloatingLongValue laserEnergyNeededPerHardness;
    public final CachedFloatingLongValue laserEnergyPerDamage;
    //Oredictionificator
    public final CachedOredictionificatorConfigValue validOredictionificatorFilters;
    //Pump
    public final CachedIntValue maxPumpRange;
    public final CachedBooleanValue pumpWaterSources;
    public final CachedIntValue maxPlenisherNodes;
    //Quantum Entangloporter
    public final CachedFloatingLongValue entangloporterEnergyBuffer;
    public final CachedIntValue entangloporterFluidBuffer;
    public final CachedLongValue entangloporterChemicalBuffer;
    //Security
    public final CachedBooleanValue allowProtection;
    public final CachedBooleanValue opsBypassRestrictions;
    //Nutritional Paste
    public final CachedFloatValue nutritionalPasteSaturation;
    public final CachedIntValue nutritionalPasteMBPerFood;
    //Thermal Evaporation Tower
    public final CachedDoubleValue evaporationHeatDissipation;
    public final CachedDoubleValue evaporationTempMultiplier;
    public final CachedDoubleValue evaporationSolarMultiplier;
    public final CachedDoubleValue evaporationHeatCapacity;
    //SPS
    public final CachedIntValue spsInputPerAntimatter;
    public final CachedFloatingLongValue spsEnergyPerInput;

    GeneralConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("General Config. This config is synced from server to client.").push("general");

        logPackets = CachedBooleanValue.wrap(this, builder.comment("Log Mekanism packet names. Debug setting.")
              .define("logPackets", false));
        allowChunkloading = CachedBooleanValue.wrap(this, builder.comment("Disable to make the anchor upgrade not do anything.")
              .define("allowChunkloading", true));
        easyMinerFilters = CachedBooleanValue.wrap(this, builder.comment("Enable this to allow dragging items from JEI into the target slot of Digital Miner filters.")
              .define("easyMinerFilters", false));
        blockDeactivationDelay = CachedIntValue.wrap(this, builder.comment("How many ticks must pass until a block's active state is synced with the client, if it has been rapidly changing.")
              .defineInRange("blockDeactivationDelay", 60, 0, 1_200));
        cardboardModBlacklist = CachedConfigValue.wrap(this, builder.comment("Any mod ids added to this list will not be able to have any of their blocks, picked up by the cardboard box. For example: [\"mekanism\"]")
              .defineListAllowEmpty(Collections.singletonList("cardboardModBlacklist"), ArrayList::new, e -> e instanceof String modid && ResourceLocation.isValidNamespace(modid)));
        transmitterAlloyUpgrade = CachedBooleanValue.wrap(this, builder.comment("Allow right clicking on Cables/Pipes/Tubes with alloys to upgrade the tier.")
              .define("transmitterAlloyUpgrade", true));
        //If this is less than 1, upgrades make machines worse. If less than 0, I don't even know.
        maxUpgradeMultiplier = CachedIntValue.wrap(this, builder.comment("Base factor for working out machine performance with upgrades - UpgradeModifier * (UpgradesInstalled/UpgradesPossible).")
              .defineInRange("maxUpgradeMultiplier", 10, 1, Integer.MAX_VALUE));
        boilerWaterConductivity = CachedDoubleValue.wrap(this, builder.comment("How much Boiler heat is immediately usable to convert water to steam.")
              .defineInRange("boilerWaterConductivity", 0.7, 0.01, 1));
        heatPerFuelTick = CachedDoubleValue.wrap(this, builder.comment("Amount of heat produced per fuel tick of a fuel's burn time in the Fuelwood Heater.")
              .defineInRange("heatPerFuelTick", 400, 0.1, 4_000_000));
        fuelwoodTickMultiplier = CachedIntValue.wrap(this, builder.comment("Number of ticks to burn an item at in a Fuelwood Heater. Use this config option to effectively make Fuelwood Heater's burn faster but produce the same amount of heat per item.")
              .defineInRange("fuelwoodTickMultiplier", 1, 1, 1_000));
        resistiveHeaterEfficiency = CachedDoubleValue.wrap(this, builder.comment("How much heat energy is created from one Joule of regular energy in the Resistive Heater.")
              .defineInRange("resistiveHeaterEfficiency", 0.6, 0, 1));
        superheatingHeatTransfer = CachedDoubleValue.wrap(this, builder.comment("Amount of heat each Boiler heating element produces.")
              .defineInRange("superheatingHeatTransfer", 16_000_000, 0.1, 1_024_000_000));
        maxSolarNeutronActivatorRate = CachedIntValue.wrap(this, builder.comment("Peak processing rate for the Solar Neutron Activator. Note: It can go higher than this value in some extreme environments.")
              .defineInRange("maxSolarNeutronActivatorRate", 64, 1, 1_024));

        builder.comment("Dynamic Tank Settings").push(DYNAMIC_TANK);
        int maxVolume = 18 * 18 * 18;
        dynamicTankFluidPerTank = CachedIntValue.wrap(this, builder.comment("Amount of fluid (mB) that each block of the dynamic tank contributes to the volume. Max = volume * fluidPerTank")
              .defineInRange("fluidPerTank", 350_000, 1, Integer.MAX_VALUE / maxVolume));
        dynamicTankChemicalPerTank = CachedLongValue.wrap(this, builder.comment("Amount of chemical (mB) that each block of the dynamic tank contributes to the volume. Max = volume * chemicalPerTank")
              .defineInRange("chemicalPerTank", 16_000_000, 1, Long.MAX_VALUE / maxVolume));
        builder.pop();

        builder.comment("Auto Eject Settings").push(EJECT_CATEGORY);
        fluidAutoEjectRate = CachedIntValue.wrap(this, builder.comment("Rate at which fluid gets auto ejected from tiles.")
              .defineInRange("fluid", 1_024, 1, Integer.MAX_VALUE));
        chemicalAutoEjectRate = CachedLongValue.wrap(this, builder.comment("Rate at which chemicals gets auto ejected from tiles.")
              .defineInRange("chemical", 1_024L, 1, Long.MAX_VALUE));
        dumpExcessKeepRatio = CachedDoubleValue.wrap(this, builder.comment("The percentage of a tank's capacity to leave contents in when set to dumping excess.")
              .defineInRange("dumpExcessKeepRatio", 0.9D, 0.001D, 1D));
        builder.pop();

        builder.comment("Prefilled Tanks").push(PREFILLED_CATEGORY);
        prefilledFluidTanks = CachedBooleanValue.wrap(this, builder.comment("Add filled creative fluid tanks to creative/JEI.")
              .define("fluidTanks", true));
        prefilledGasTanks = CachedBooleanValue.wrap(this, builder.comment("Add filled creative gas tanks to creative/JEI.")
              .define("gasTanks", true));
        prefilledInfusionTanks = CachedBooleanValue.wrap(this, builder.comment("Add filled creative infusion tanks to creative/JEI.")
              .define("infusionTanks", true));
        prefilledPigmentTanks = CachedBooleanValue.wrap(this, builder.comment("Add filled creative pigment tanks to creative/JEI.")
              .define("pigmentTanks", true));
        prefilledSlurryTanks = CachedBooleanValue.wrap(this, builder.comment("Add filled creative slurry tanks to creative/JEI.")
              .define("slurryTanks", true));
        builder.pop();

        builder.comment("Energy Conversion Rate Settings").push(CONVERSION_CATEGORY);
        blacklistIC2 = CachedBooleanValue.wrap(this, builder.comment("Disables IC2 power integration. Requires world restart (server-side option in SMP).")
              .worldRestart()
              .define("blacklistIC2", false));
        //TODO - 1.20: Rename config entry
        ic2ConversionRate = CachedFloatingLongValue.define(this, builder, "Conversion multiplier from EU to Joules (EU * JoulePerEU = Joules)",
              "JoulePerEU", FloatingLong.createConst(10), CachedFloatingLongValue.ENERGY_CONVERSION);
        blacklistForge = CachedBooleanValue.wrap(this, builder.comment("Disables Forge Energy (FE,RF,IF,uF,CF) power integration. Requires world restart (server-side option in SMP).")
              .worldRestart()
              .define("blacklistForge", false));
        //TODO - 1.20: Rename config entry
        forgeConversionRate = CachedFloatingLongValue.define(this, builder, "Conversion multiplier from Forge Energy to Joules (FE * JoulePerForgeEnergy = Joules)",
              "JoulePerForgeEnergy", FloatingLong.createConst(2.5), CachedFloatingLongValue.ENERGY_CONVERSION);
        blacklistFluxNetworks = CachedBooleanValue.wrap(this, builder.comment("Disables Flux Networks higher throughput Forge Energy (FE,RF,IF,uF,CF) power integration. Requires world restart (server-side option in SMP). Note: Disabling Forge Energy integration also disables this.")
              .worldRestart()
              .define("blacklistFluxNetworks", false));
        FROM_H2 = CachedFloatingLongValue.define(this, builder, "How much energy is produced per mB of Hydrogen, also affects Electrolytic Separator usage, Ethylene burn rate and Gas generator energy capacity.",
              "HydrogenEnergyDensity", FloatingLong.createConst(200), CachedFloatingLongValue.POSITIVE);
        ETHENE_BURN_TIME = CachedIntValue.wrap(this, builder.comment("Burn time for Ethylene (1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus).")
              .defineInRange("EthyleneBurnTime", 40, 1, Integer.MAX_VALUE));
        maxEnergyPerSteam = CachedFloatingLongValue.define(this, builder, "Maximum Joules per mB of Steam. Also affects Thermoelectric Boiler.",
              "maxEnergyPerSteam", FloatingLong.createConst(10));
        builder.pop();

        builder.comment("Radiation Settings").push(RADIATION_CATEGORY);
        radiationEnabled = CachedBooleanValue.wrap(this, builder.comment("Enable worldwide radiation effects. Don't be a downer and disable this.")
              .define("radiationEnabled", true));
        radiationChunkCheckRadius = CachedIntValue.wrap(this, builder.comment("The radius of chunks checked when running radiation calculations. The algorithm is efficient, but don't abuse it by making this crazy high.")
              .defineInRange("chunkCheckRadius", 5, 1, 100));
        radiationSourceDecayRate = CachedDoubleValue.wrap(this, builder.comment("Radiation sources are multiplied by this constant roughly once per second to represent their emission decay. At the default rate, it takes roughly 10 hours to remove a 1,000 Sv/h (crazy high) source.")
              .defineInRange("sourceDecayRate", 0.9995D, 0, 1));
        radiationTargetDecayRate = CachedDoubleValue.wrap(this, builder.comment("Radiated objects and entities are multiplied by this constant roughly once per second to represent their dosage decay.")
              .defineInRange("targetDecayRate", 0.9995D, 0, 1));
        radiationNegativeEffectsMinSeverity = CachedDoubleValue.wrap(this, builder.comment("Defines the minimum severity radiation dosage severity (scale of 0 to 1) for which negative effects can take place. Set to 1 to disable negative effects completely.")
              .defineInRange("negativeEffectsMinSeverity", 0.1D, 0, 1));
        radioactiveWasteBarrelMaxGas = CachedLongValue.wrap(this, builder.comment("Amount of gas (mB) that can be stored in a Radioactive Waste Barrel.")
              .defineInRange("radioactiveWasteBarrelMaxGas", 512_000, 1, Long.MAX_VALUE));
        radioactiveWasteBarrelProcessTicks = CachedIntValue.wrap(this, builder.comment("Number of ticks required for radioactive gas stored in a Radioactive Waste Barrel to decay radioactiveWasteBarrelDecayAmount mB.")
              .defineInRange("radioactiveWasteBarrelProcessTicks", 20, 1, Integer.MAX_VALUE));
        radioactiveWasteBarrelDecayAmount = CachedLongValue.wrap(this, builder.comment("Number of mB of gas that decay every radioactiveWasteBarrelProcessTicks ticks when stored in a Radioactive Waste Barrel. Set to zero to disable decay all together. (Gases in the mekanism:waste_barrel_decay_blacklist tag will not decay).")
              .defineInRange("radioactiveWasteBarrelDecayAmount", 1, 0, Long.MAX_VALUE));
        builder.pop();

        builder.comment("Digital Miner Settings").push(MINER_CATEGORY);
        minerSilkMultiplier = CachedIntValue.wrap(this, builder.comment("Energy multiplier for using silk touch mode with the Digital Miner.")
              .defineInRange("silkMultiplier", 12, 1, Integer.MAX_VALUE));
        minerMaxRadius = CachedIntValue.wrap(this, builder.comment("Maximum radius in blocks that the Digital Miner can reach. (Increasing this may have negative effects on stability and/or performance. We strongly recommend you leave it at the default value).")
              .defineInRange("maxRadius", 32, 1, Integer.MAX_VALUE));
        minerTicksPerMine = CachedIntValue.wrap(this, builder.comment("Number of ticks required to mine a single block with a Digital Miner (without any upgrades).")
              .defineInRange("ticksPerMine", 80, 1, Integer.MAX_VALUE));
        builder.pop();

        builder.comment("Laser Settings").push(LASER_SETTINGS);
        aestheticWorldDamage = CachedBooleanValue.wrap(this, builder.comment("If enabled, lasers can break blocks and the flamethrower starts fires.")
              .define("aestheticWorldDamage", true));
        laserRange = CachedIntValue.wrap(this, builder.comment("How far (in blocks) a laser can travel.")
              .defineInRange("range", 64, 1, 1_024));
        laserEnergyNeededPerHardness = CachedFloatingLongValue.define(this, builder, "Energy needed to destroy or attract blocks with a Laser (per block hardness level).",
              "energyNeededPerHardness", FloatingLong.createConst(100_000));
        laserEnergyPerDamage = CachedFloatingLongValue.define(this, builder, "Energy used per half heart of damage being transferred to entities.",
              "energyPerDamage", FloatingLong.createConst(2_500), CachedFloatingLongValue.POSITIVE);
        builder.pop();

        builder.comment("Oredictionificator Settings").push(OREDICTIONIFICATOR_CATEGORY);
        validOredictionificatorFilters = CachedOredictionificatorConfigValue.define(this, builder.comment("The list of valid tag prefixes for the Oredictionificator. Note: It is highly recommended to only include well known/defined tag prefixes otherwise it is very easy to potentially add in accidental conversions of things that are not actually equivalent."),
              "validItemFilters", () -> Collections.singletonMap("forge", List.of("ingots/", "ores/", "dusts/", "nuggets/", "storage_blocks/")));
        builder.pop();

        builder.comment("Pump Settings").push(PUMP_CATEGORY);
        maxPumpRange = CachedIntValue.wrap(this, builder.comment("Maximum block distance to pull fluid from for the Electric Pump.")
              .defineInRange("maxPumpRange", 80, 1, 512));
        pumpWaterSources = CachedBooleanValue.wrap(this, builder.comment("If enabled makes Water and Heavy Water blocks be removed from the world on pump.")
              .define("pumpWaterSources", false));
        maxPlenisherNodes = CachedIntValue.wrap(this, builder.comment("Fluidic Plenisher stops after this many blocks.")
              .defineInRange("maxPlenisherNodes", 4_000, 1, 1_000_000));
        builder.pop();

        builder.comment("Quantum Entangloporter Settings").push(ENTANGLOPORTER_CATEGORY);
        entangloporterEnergyBuffer = CachedFloatingLongValue.define(this, builder, "Maximum energy buffer (Mekanism Joules) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier energy cube capacity.",
              "energyBuffer", EnergyCubeTier.ULTIMATE.getBaseMaxEnergy(), true, CachedFloatingLongValue.POSITIVE);
        entangloporterFluidBuffer = CachedIntValue.wrap(this, builder.comment("Maximum fluid buffer (mb) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier tank capacity.")
              .worldRestart()
              .defineInRange("fluidBuffer", FluidTankTier.ULTIMATE.getBaseStorage(), 1, Integer.MAX_VALUE));
        entangloporterChemicalBuffer = CachedLongValue.wrap(this, builder.comment("Maximum chemical buffer (mb) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier tank capacity.")
              .worldRestart()
              .defineInRange("chemicalBuffer", ChemicalTankTier.ULTIMATE.getBaseStorage(), 1, Long.MAX_VALUE));
        builder.pop();

        builder.comment("Block security/protection Settings").push(SECURITY_CATEGORY);
        allowProtection = CachedBooleanValue.wrap(this, builder.comment("Enable the security system for players to prevent others from accessing their machines. Does NOT affect Frequencies.")
              .define("allowProtection", true));
        opsBypassRestrictions = CachedBooleanValue.wrap(this, builder.comment("If this is enabled then players with the 'mekanism.bypass_security' permission (default ops) can bypass the block and item security restrictions.")
              .define("opsBypassRestrictions", false));
        builder.pop();

        builder.comment("Nutritional Paste Settings").push(NUTRITIONAL_PASTE_CATEGORY);
        nutritionalPasteSaturation = CachedFloatValue.wrap(this, builder.comment("Saturation level of Nutritional Paste when eaten.")
              .defineInRange("saturation", 0.8, 0, 100));
        nutritionalPasteMBPerFood = CachedIntValue.wrap(this, builder.comment("How much mB of Nutritional Paste equates to one 'half-food.'")
              .defineInRange("mbPerFood", 50, 1, Integer.MAX_VALUE));
        builder.pop();

        builder.comment("Thermal Evaporation Plant Settings").push(EVAPORATION_CATEGORY);
        evaporationHeatDissipation = CachedDoubleValue.wrap(this, builder.comment("Thermal Evaporation Tower heat loss per tick.")
              .defineInRange("heatDissipation", 0.02, 0.001, 1_000));
        evaporationTempMultiplier = CachedDoubleValue.wrap(this, builder.comment("Temperature to amount produced ratio for Thermal Evaporation Tower.")
              .defineInRange("tempMultiplier", 0.4, 0.001, 1_000_000));
        evaporationSolarMultiplier = CachedDoubleValue.wrap(this, builder.comment("Heat to absorb per Solar Panel array of Thermal Evaporation Tower.")
              .defineInRange("solarMultiplier", 0.2, 0.001, 1_000_000));
        evaporationHeatCapacity = CachedDoubleValue.wrap(this, builder.comment("Heat capacity of Thermal Evaporation Tower layers (increases amount of energy needed to increase temperature).")
              .defineInRange("heatCapacity", 100D, 1, 1_000_000));
        builder.pop();

        builder.comment("SPS Settings").push(SPS_CATEGORY);
        spsInputPerAntimatter = CachedIntValue.wrap(this, builder.comment("How much input gas (polonium) in mB must be processed to make 1 mB of antimatter.")
              .defineInRange("inputPerAntimatter", 1_000, 1, Integer.MAX_VALUE));
        spsEnergyPerInput = CachedFloatingLongValue.define(this, builder, "Energy needed to process 1 mB of input (inputPerAntimatter * energyPerInput = energy to produce 1 mB of antimatter).",
              "energyPerInput", FloatingLong.createConst(1_000_000));
        builder.pop();

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "general";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}