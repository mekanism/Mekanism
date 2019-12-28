package mekanism.client;

import mekanism.api.gas.Slurry;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.client.lang.BaseLanguageProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismGases;
import mekanism.common.MekanismInfuseTypes;
import mekanism.common.MekanismItem;
import mekanism.common.MekanismLang;
import mekanism.common.entity.MekanismEntityTypes;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.SlurryRegistryObject;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.BucketItem;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;

public class MekanismLangGenerator extends BaseLanguageProvider {

    public MekanismLangGenerator(DataGenerator gen) {
        super(gen, Mekanism.MODID);
    }

    @Override
    protected void addTranslations() {
        addItems();
        addBlocks();
        addFluids();
        addEntities();
        addGases();
        addInfusionTypes();
        addMisc();
    }

    private void addItems() {
        add(MekanismItem.ROBIT, "Robit");
        add(MekanismItem.ENERGY_TABLET, "Energy Tablet");
        add(MekanismItem.CONFIGURATION_CARD, "Configuration Card");
        add(MekanismItem.CRAFTING_FORMULA, "Crafting Formula");
        add(MekanismItem.TELEPORTATION_CORE, "Teleportation Core");
        add(MekanismItem.ENRICHED_IRON, "Enriched Iron");
        add(MekanismItem.ELECTROLYTIC_CORE, "Electrolytic Core");
        add(MekanismItem.SAWDUST, "Sawdust");
        add(MekanismItem.SALT, "Salt");
        add(MekanismItem.SUBSTRATE, "Substrate");
        add(MekanismItem.BIO_FUEL, "Bio Fuel");
        //Not sure this is even used
        add(MekanismItem.ITEM_PROXY, "Item Proxy");
        //Tools/Armor
        add(MekanismItem.GAUGE_DROPPER, "Gauge Dropper");
        add(MekanismItem.DICTIONARY, "Dictionary");
        add(MekanismItem.CONFIGURATOR, "Configurator");
        add(MekanismItem.NETWORK_READER, "Network Reader");
        add(MekanismItem.SEISMIC_READER, "Seismic Reader");
        add(MekanismItem.PORTABLE_TELEPORTER, "Portable Teleporter");
        add(MekanismItem.ELECTRIC_BOW, "Electric Bow");
        add(MekanismItem.ATOMIC_DISASSEMBLER, "Atomic Disassembler");
        add(MekanismItem.GAS_MASK, "Gas Mask");
        add(MekanismItem.SCUBA_TANK, "Scuba Tank");
        add(MekanismItem.FLAMETHROWER, "Flamethrower");
        add(MekanismItem.FREE_RUNNERS, "Free Runners");
        add(MekanismItem.JETPACK, "Jetpack");
        add(MekanismItem.ARMORED_JETPACK, "Armored Jetpack");
        //HDPE
        add(MekanismItem.HDPE_PELLET, "HDPE Pellet");
        add(MekanismItem.HDPE_ROD, "HDPE Rod");
        add(MekanismItem.HDPE_SHEET, "HDPE Sheet");
        add(MekanismItem.HDPE_STICK, "PlaStick");
        //Enriched Items
        add(MekanismItem.ENRICHED_CARBON, "Enriched Carbon");
        add(MekanismItem.ENRICHED_REDSTONE, "Enriched Redstone");
        add(MekanismItem.ENRICHED_DIAMOND, "Enriched Diamond");
        add(MekanismItem.ENRICHED_OBSIDIAN, "Enriched Obsidian");
        add(MekanismItem.ENRICHED_TIN, "Enriched Tin");
        //Upgrades
        add(MekanismItem.SPEED_UPGRADE, "Speed Upgrade");
        add(MekanismItem.ENERGY_UPGRADE, "Energy Upgrade");
        add(MekanismItem.FILTER_UPGRADE, "Filter Upgrade");
        add(MekanismItem.MUFFLING_UPGRADE, "Muffling Upgrade");
        add(MekanismItem.GAS_UPGRADE, "Gas Upgrade");
        add(MekanismItem.ANCHOR_UPGRADE, "Anchor Upgrade");
        //Alloys
        add(MekanismItem.INFUSED_ALLOY, "Infused Alloy");
        add(MekanismItem.REINFORCED_ALLOY, "Reinforced Alloy");
        add(MekanismItem.ATOMIC_ALLOY, "Atomic Alloy");
        //Ingots
        add(MekanismItem.REFINED_OBSIDIAN_INGOT, "Refined Obsidian Ingot");
        add(MekanismItem.OSMIUM_INGOT, "Osmium Ingot");
        add(MekanismItem.BRONZE_INGOT, "Bronze Ingot");
        add(MekanismItem.REFINED_GLOWSTONE_INGOT, "Refined Glowstone Ingot");
        add(MekanismItem.STEEL_INGOT, "Steel Ingot");
        add(MekanismItem.COPPER_INGOT, "Copper Ingot");
        add(MekanismItem.TIN_INGOT, "Tin Ingot");
        //Nuggets
        add(MekanismItem.REFINED_OBSIDIAN_NUGGET, "Refined Obsidian Nugget");
        add(MekanismItem.OSMIUM_NUGGET, "Osmium Nugget");
        add(MekanismItem.BRONZE_NUGGET, "Bronze Nugget");
        add(MekanismItem.REFINED_GLOWSTONE_NUGGET, "Refined Glowstone Nugget");
        add(MekanismItem.STEEL_NUGGET, "Steel Nugget");
        add(MekanismItem.COPPER_NUGGET, "Copper Nugget");
        add(MekanismItem.TIN_NUGGET, "Tin Nugget");
        //Dusts
        add(MekanismItem.BRONZE_DUST, "Bronze Dust");
        add(MekanismItem.LAPIS_LAZULI_DUST, "Lapis Lazuli Dust");
        add(MekanismItem.COAL_DUST, "Coal Dust");
        add(MekanismItem.CHARCOAL_DUST, "Charcoal Dust");
        add(MekanismItem.QUARTZ_DUST, "Quartz Dust");
        add(MekanismItem.EMERALD_DUST, "Emerald Dust");
        add(MekanismItem.DIAMOND_DUST, "Diamond Dust");
        add(MekanismItem.STEEL_DUST, "Steel Dust");
        add(MekanismItem.SULFUR_DUST, "Sulfur Dust");
        add(MekanismItem.LITHIUM_DUST, "Lithium Dust");
        add(MekanismItem.REFINED_OBSIDIAN_DUST, "Refined Obsidian Dust");
        add(MekanismItem.OBSIDIAN_DUST, "Obsidian Dust");
        //Tiered stuff
        addTiered(MekanismItem.BASIC_CONTROL_CIRCUIT, MekanismItem.ADVANCED_CONTROL_CIRCUIT, MekanismItem.ELITE_CONTROL_CIRCUIT, MekanismItem.ULTIMATE_CONTROL_CIRCUIT, "Control Circuit");
        addTiered(MekanismItem.BASIC_TIER_INSTALLER, MekanismItem.ADVANCED_TIER_INSTALLER, MekanismItem.ELITE_TIER_INSTALLER, MekanismItem.ULTIMATE_TIER_INSTALLER, "Tier Installer");
        //Ore processing parts
        addOreProcessingNames(MekanismItem.IRON_CRYSTAL, MekanismItem.IRON_SHARD, MekanismItem.IRON_CLUMP, MekanismItem.DIRTY_IRON_DUST, MekanismItem.IRON_DUST, "Iron");
        addOreProcessingNames(MekanismItem.GOLD_CRYSTAL, MekanismItem.GOLD_SHARD, MekanismItem.GOLD_CLUMP, MekanismItem.DIRTY_GOLD_DUST, MekanismItem.GOLD_DUST, "Gold");
        addOreProcessingNames(MekanismItem.OSMIUM_CRYSTAL, MekanismItem.OSMIUM_SHARD, MekanismItem.OSMIUM_CLUMP, MekanismItem.DIRTY_OSMIUM_DUST, MekanismItem.OSMIUM_DUST, "Osmium");
        addOreProcessingNames(MekanismItem.COPPER_CRYSTAL, MekanismItem.COPPER_SHARD, MekanismItem.COPPER_CLUMP, MekanismItem.DIRTY_COPPER_DUST, MekanismItem.COPPER_DUST, "Copper");
        addOreProcessingNames(MekanismItem.TIN_CRYSTAL, MekanismItem.TIN_SHARD, MekanismItem.TIN_CLUMP, MekanismItem.DIRTY_TIN_DUST, MekanismItem.TIN_DUST, "Tin");
    }

    private void addBlocks() {
        add(MekanismBlock.BOILER_CASING, "Boiler Casing");
        add(MekanismBlock.BOILER_VALVE, "Boiler Valve");
        add(MekanismBlock.CARDBOARD_BOX, "Cardboard Box");
        add(MekanismBlock.CHARGEPAD, "Chargepad");
        add(MekanismBlock.CHEMICAL_CRYSTALLIZER, "Chemical Crystallizer");
        add(MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, "Chemical Dissolution Chamber");
        add(MekanismBlock.CHEMICAL_INFUSER, "Chemical Infuser");
        add(MekanismBlock.CHEMICAL_INJECTION_CHAMBER, "Chemical Injection Chamber");
        add(MekanismBlock.CHEMICAL_OXIDIZER, "Chemical Oxidizer");
        add(MekanismBlock.CHEMICAL_WASHER, "Chemical Washer");
        add(MekanismBlock.COMBINER, "Combiner");
        add(MekanismBlock.CRUSHER, "Crusher");
        add(MekanismBlock.DIGITAL_MINER, "Digital Miner");
        add(MekanismBlock.DYNAMIC_TANK, "Dynamic Tank");
        add(MekanismBlock.DYNAMIC_VALVE, "Dynamic Valve");
        add(MekanismBlock.ELECTRIC_PUMP, "Electric Pump");
        add(MekanismBlock.ELECTROLYTIC_SEPARATOR, "Electrolytic Separator");
        add(MekanismBlock.ENERGIZED_SMELTER, "Energized Smelter");
        add(MekanismBlock.ENRICHMENT_CHAMBER, "Enrichment Chamber");
        add(MekanismBlock.FLUIDIC_PLENISHER, "Fluidic Plenisher");
        add(MekanismBlock.FORMULAIC_ASSEMBLICATOR, "Formulaic Assemblicator");
        add(MekanismBlock.FUELWOOD_HEATER, "Fuelwood Heater");
        add(MekanismBlock.INDUCTION_CASING, "Induction Casing");
        add(MekanismBlock.INDUCTION_PORT, "Induction Port");
        add(MekanismBlock.LASER, "Laser");
        add(MekanismBlock.LASER_AMPLIFIER, "Laser Amplifier");
        add(MekanismBlock.LASER_TRACTOR_BEAM, "Laser Tractor Beam");
        add(MekanismBlock.LOGISTICAL_SORTER, "Logistical Sorter");
        add(MekanismBlock.METALLURGIC_INFUSER, "Metallurgic Infuser");
        add(MekanismBlock.OREDICTIONIFICATOR, "Oredictionificator");
        add(MekanismBlock.OSMIUM_COMPRESSOR, "Osmium Compressor");
        add(MekanismBlock.PERSONAL_CHEST, "Personal Chest");
        add(MekanismBlock.PRECISION_SAWMILL, "Precision Sawmill");
        add(MekanismBlock.PRESSURE_DISPERSER, "Pressure Disperser");
        add(MekanismBlock.PRESSURIZED_REACTION_CHAMBER, "Pressurized Reaction Chamber");
        add(MekanismBlock.PURIFICATION_CHAMBER, "Purification Chamber");
        add(MekanismBlock.QUANTUM_ENTANGLOPORTER, "Quantum Entangloporter");
        add(MekanismBlock.RESISTIVE_HEATER, "Resistive Heater");
        add(MekanismBlock.ROTARY_CONDENSENTRATOR, "Rotary Condensentrator");
        add(MekanismBlock.SALT_BLOCK, "Salt Block");
        add(MekanismBlock.SECURITY_DESK, "Security Desk");
        add(MekanismBlock.SEISMIC_VIBRATOR, "Seismic Vibrator");
        add(MekanismBlock.SOLAR_NEUTRON_ACTIVATOR, "Solar Neutron Activator");
        add(MekanismBlock.STEEL_CASING, "Steel Casing");
        add(MekanismBlock.STRUCTURAL_GLASS, "Structural Glass");
        add(MekanismBlock.SUPERHEATING_ELEMENT, "Superheating Element");
        add(MekanismBlock.TELEPORTER, "Teleporter");
        add(MekanismBlock.TELEPORTER_FRAME, "Teleporter Frame");
        add(MekanismBlock.THERMAL_EVAPORATION_BLOCK, "Thermal Evaporation Block");
        add(MekanismBlock.THERMAL_EVAPORATION_CONTROLLER, "Thermal Evaporation Controller");
        add(MekanismBlock.THERMAL_EVAPORATION_VALVE, "Thermal Evaporation Valve");
        //Bounding block (I don't think these lang keys actually will ever be used, but set them just in case)
        add(MekanismBlock.BOUNDING_BLOCK, "Bounding Block");
        add(MekanismBlock.ADVANCED_BOUNDING_BLOCK, "Advanced Bounding Block");
        //Ores
        add(MekanismBlock.OSMIUM_ORE, "Osmium Ore");
        add(MekanismBlock.COPPER_ORE, "Copper Ore");
        add(MekanismBlock.TIN_ORE, "Tin Ore");
        //Storage blocks
        add(MekanismBlock.OSMIUM_BLOCK, "Osmium Block");
        add(MekanismBlock.BRONZE_BLOCK, "Bronze Block");
        add(MekanismBlock.REFINED_OBSIDIAN_BLOCK, "Refined Obsidian");
        add(MekanismBlock.CHARCOAL_BLOCK, "Charcoal Block");
        add(MekanismBlock.REFINED_GLOWSTONE_BLOCK, "Refined Glowstone");
        add(MekanismBlock.STEEL_BLOCK, "Steel Block");
        add(MekanismBlock.COPPER_BLOCK, "Copper Block");
        add(MekanismBlock.TIN_BLOCK, "Tin Block");
        //Tiered things
        addTiered(MekanismBlock.BASIC_INDUCTION_CELL, MekanismBlock.ADVANCED_INDUCTION_CELL, MekanismBlock.ELITE_INDUCTION_CELL, MekanismBlock.ULTIMATE_INDUCTION_CELL, "Induction Cell");
        addTiered(MekanismBlock.BASIC_INDUCTION_PROVIDER, MekanismBlock.ADVANCED_INDUCTION_PROVIDER, MekanismBlock.ELITE_INDUCTION_PROVIDER, MekanismBlock.ULTIMATE_INDUCTION_PROVIDER, "Induction Provider");
        addTiered(MekanismBlock.BASIC_BIN, MekanismBlock.ADVANCED_BIN, MekanismBlock.ELITE_BIN, MekanismBlock.ULTIMATE_BIN, MekanismBlock.CREATIVE_BIN, "Bin");
        addTiered(MekanismBlock.BASIC_ENERGY_CUBE, MekanismBlock.ADVANCED_ENERGY_CUBE, MekanismBlock.ELITE_ENERGY_CUBE, MekanismBlock.ULTIMATE_ENERGY_CUBE, MekanismBlock.CREATIVE_ENERGY_CUBE, "Energy Cube");
        addTiered(MekanismBlock.BASIC_FLUID_TANK, MekanismBlock.ADVANCED_FLUID_TANK, MekanismBlock.ELITE_FLUID_TANK, MekanismBlock.ULTIMATE_FLUID_TANK, MekanismBlock.CREATIVE_FLUID_TANK, "Fluid Tank");
        addTiered(MekanismBlock.BASIC_GAS_TANK, MekanismBlock.ADVANCED_GAS_TANK, MekanismBlock.ELITE_GAS_TANK, MekanismBlock.ULTIMATE_GAS_TANK, MekanismBlock.CREATIVE_GAS_TANK, "Gas Tank");
        //Factories
        addTiered(MekanismBlock.BASIC_SMELTING_FACTORY, MekanismBlock.ADVANCED_SMELTING_FACTORY, MekanismBlock.ELITE_SMELTING_FACTORY, MekanismBlock.ULTIMATE_SMELTING_FACTORY, "Smelting Factory");
        addTiered(MekanismBlock.BASIC_ENRICHING_FACTORY, MekanismBlock.ADVANCED_ENRICHING_FACTORY, MekanismBlock.ELITE_ENRICHING_FACTORY, MekanismBlock.ULTIMATE_ENRICHING_FACTORY, "Enriching Factory");
        addTiered(MekanismBlock.BASIC_CRUSHING_FACTORY, MekanismBlock.ADVANCED_CRUSHING_FACTORY, MekanismBlock.ELITE_CRUSHING_FACTORY, MekanismBlock.ULTIMATE_CRUSHING_FACTORY, "Crushing Factory");
        addTiered(MekanismBlock.BASIC_COMPRESSING_FACTORY, MekanismBlock.ADVANCED_COMPRESSING_FACTORY, MekanismBlock.ELITE_COMPRESSING_FACTORY, MekanismBlock.ULTIMATE_COMPRESSING_FACTORY, "Compressing Factory");
        addTiered(MekanismBlock.BASIC_COMBINING_FACTORY, MekanismBlock.ADVANCED_COMBINING_FACTORY, MekanismBlock.ELITE_COMBINING_FACTORY, MekanismBlock.ULTIMATE_COMBINING_FACTORY, "Combining Factory");
        addTiered(MekanismBlock.BASIC_PURIFYING_FACTORY, MekanismBlock.ADVANCED_PURIFYING_FACTORY, MekanismBlock.ELITE_PURIFYING_FACTORY, MekanismBlock.ULTIMATE_PURIFYING_FACTORY, "Purifying Factory");
        addTiered(MekanismBlock.BASIC_INJECTING_FACTORY, MekanismBlock.ADVANCED_INJECTING_FACTORY, MekanismBlock.ELITE_INJECTING_FACTORY, MekanismBlock.ULTIMATE_INJECTING_FACTORY, "Injecting Factory");
        addTiered(MekanismBlock.BASIC_INFUSING_FACTORY, MekanismBlock.ADVANCED_INFUSING_FACTORY, MekanismBlock.ELITE_INFUSING_FACTORY, MekanismBlock.ULTIMATE_INFUSING_FACTORY, "Infusing Factory");
        addTiered(MekanismBlock.BASIC_SAWING_FACTORY, MekanismBlock.ADVANCED_SAWING_FACTORY, MekanismBlock.ELITE_SAWING_FACTORY, MekanismBlock.ULTIMATE_SAWING_FACTORY, "Sawing Factory");
        //Transmitters
        add(MekanismBlock.RESTRICTIVE_TRANSPORTER, "Restrictive Transporter");
        add(MekanismBlock.DIVERSION_TRANSPORTER, "Diversion Transporter");
        addTiered(MekanismBlock.BASIC_UNIVERSAL_CABLE, MekanismBlock.ADVANCED_UNIVERSAL_CABLE, MekanismBlock.ELITE_UNIVERSAL_CABLE, MekanismBlock.ULTIMATE_UNIVERSAL_CABLE, "Universal Cable");
        addTiered(MekanismBlock.BASIC_MECHANICAL_PIPE, MekanismBlock.ADVANCED_MECHANICAL_PIPE, MekanismBlock.ELITE_MECHANICAL_PIPE, MekanismBlock.ULTIMATE_MECHANICAL_PIPE, "Mechanical Pipe");
        addTiered(MekanismBlock.BASIC_PRESSURIZED_TUBE, MekanismBlock.ADVANCED_PRESSURIZED_TUBE, MekanismBlock.ELITE_PRESSURIZED_TUBE, MekanismBlock.ULTIMATE_PRESSURIZED_TUBE, "Pressurized Tube");
        addTiered(MekanismBlock.BASIC_LOGISTICAL_TRANSPORTER, MekanismBlock.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismBlock.ELITE_LOGISTICAL_TRANSPORTER, MekanismBlock.ULTIMATE_LOGISTICAL_TRANSPORTER, "Logistical Transporter");
        addTiered(MekanismBlock.BASIC_THERMODYNAMIC_CONDUCTOR, MekanismBlock.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismBlock.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismBlock.ULTIMATE_THERMODYNAMIC_CONDUCTOR, "Thermodynamic Conductor");
    }

    private void addFluids() {
        addFluid(MekanismFluids.HYDROGEN, "Liquid Hydrogen");
        addFluid(MekanismFluids.OXYGEN, "Liquid Oxygen");
        addFluid(MekanismFluids.CHLORINE, "Liquid Chlorine");
        addFluid(MekanismFluids.SULFUR_DIOXIDE, "Liquid Sulfur Dioxide");
        addFluid(MekanismFluids.SULFUR_TRIOXIDE, "Liquid Sulfur Trioxide");
        addFluid(MekanismFluids.SULFURIC_ACID, "Sulfuric Acid");
        addFluid(MekanismFluids.HYDROGEN_CHLORIDE, "Liquid Hydrogen Chloride");
        addFluid(MekanismFluids.ETHENE, "Liquid Ethylene");
        addFluid(MekanismFluids.SODIUM, "Liquid Sodium");
        addFluid(MekanismFluids.BRINE, "Brine");
        addFluid(MekanismFluids.DEUTERIUM, "Liquid Deuterium");
        addFluid(MekanismFluids.TRITIUM, "Liquid Tritium");
        addFluid(MekanismFluids.FUSION_FUEL, "Liquid D-T Fuel");
        addFluid(MekanismFluids.LITHIUM, "Liquid Lithium");
        addFluid(MekanismFluids.STEAM, "Liquid Steam");
        addFluid(MekanismFluids.HEAVY_WATER, "Heavy Water");
    }

    private void addEntities() {
        add(MekanismEntityTypes.FLAME, "Flamethrower Flame");
        add(MekanismEntityTypes.ROBIT, "Robit");
    }

    private void addGases() {
        add(MekanismGases.HYDROGEN, "Hydrogen");
        add(MekanismGases.OXYGEN, "Oxygen");
        add(MekanismGases.STEAM, "Steam");
        add(MekanismGases.CHLORINE, "Chlorine");
        add(MekanismGases.SULFUR_DIOXIDE, "Sulfur Dioxide");
        add(MekanismGases.SULFUR_TRIOXIDE, "Sulfur Trioxide");
        add(MekanismGases.SULFURIC_ACID, "Sulfuric Acid");
        add(MekanismGases.HYDROGEN_CHLORIDE, "Hydrogen Chloride");
        add(MekanismGases.ETHENE, "Ethylene");
        add(MekanismGases.SODIUM, "Sodium");
        add(MekanismGases.BRINE, "Gaseous Brine");
        add(MekanismGases.DEUTERIUM, "Deuterium");
        add(MekanismGases.TRITIUM, "Tritium");
        add(MekanismGases.FUSION_FUEL, "D-T Fuel");
        add(MekanismGases.LITHIUM, "Lithium");
        add(MekanismGases.LIQUID_OSMIUM, "Liquid Osmium");
        //Slurry
        addSlurry(MekanismGases.IRON_SLURRY, "Iron");
        addSlurry(MekanismGases.GOLD_SLURRY, "Gold");
        addSlurry(MekanismGases.OSMIUM_SLURRY, "Osmium");
        addSlurry(MekanismGases.COPPER_SLURRY, "Copper");
        addSlurry(MekanismGases.TIN_SLURRY, "Tin");
    }

    private void addInfusionTypes() {
        add(MekanismInfuseTypes.CARBON, "Carbon");
        add(MekanismInfuseTypes.REDSTONE, "Redstone");
        add(MekanismInfuseTypes.DIAMOND, "Diamond");
        add(MekanismInfuseTypes.REFINED_OBSIDIAN, "Refined Obsidian");
        add(MekanismInfuseTypes.TIN, "Tin");
        add(MekanismInfuseTypes.FUNGI, "Fungi");
        add(MekanismInfuseTypes.BIO, "Biomass");
    }

    private void addMisc() {
        //Upgrades
        add(APILang.UPGRADE_SPEED, "Speed");
        add(APILang.UPGRADE_SPEED_DESCRIPTION, "Increases speed of machinery.");
        add(APILang.UPGRADE_ENERGY, "Energy");
        add(APILang.UPGRADE_ENERGY_DESCRIPTION, "Increases energy efficiency and capacity of machinery.");
        add(APILang.UPGRADE_FILTER, "Filter");
        add(APILang.UPGRADE_FILTER_DESCRIPTION, "A filter that separates heavy water from regular water.");
        add(APILang.UPGRADE_GAS, "Gas");
        add(APILang.UPGRADE_GAS_DESCRIPTION, "Increases the efficiency of gas-using machinery.");
        add(APILang.UPGRADE_MUFFLING, "Muffling");
        add(APILang.UPGRADE_MUFFLING_DESCRIPTION, "Reduces noise generated by machinery.");
        add(APILang.UPGRADE_ANCHOR, "Anchor");
        add(APILang.UPGRADE_ANCHOR_DESCRIPTION, "Keeps a machine's chunk loaded.");
        //Transmission types
        add(APILang.TRANSMISSION_TYPE_ENERGY, "Energy");
        add(APILang.TRANSMISSION_TYPE_FLUID, "Fluids");
        add(APILang.TRANSMISSION_TYPE_GAS, "Gases");
        add(APILang.TRANSMISSION_TYPE_ITEM, "Items");
        add(APILang.TRANSMISSION_TYPE_HEAT, "Heat");
        //Colors
        for (EnumColor color : EnumColor.values()) {
            add(color.getLangEntry(), color.getEnglishName());
        }
        add(MekanismLang.MEKANISM, Mekanism.MOD_NAME);
        add(MekanismLang.DEBUG_TITLE, Mekanism.MOD_NAME + " Debug");
        add(MekanismLang.LOG_FORMAT, "[%s] %s");
        add(MekanismLang.FORGE, "MinecraftForge");
        add(MekanismLang.IC2, "IndustrialCraft");
        add(MekanismLang.ERROR, "Error");
        add(MekanismLang.ALPHA_WARNING, "Warning: Mekanism is currently in alpha, and is not recommended for widespread use in modpacks. There are likely to be game breaking bugs, and various other issues that you can read more about %s.");
        add(MekanismLang.ALPHA_WARNING_HERE, "here");
        //JEI
        add(MekanismLang.JEI_AMOUNT_WITH_CAPACITY, "%s / %s mB");
        //Key
        add(MekanismLang.KEY_MODE, "Item Mode Switch");
        add(MekanismLang.KEY_ARMOR_MODE, "Armor Mode Switch");
        add(MekanismLang.KEY_FEET_MODE, "Feet Mode Switch");
        //Holiday
        add(MekanismLang.HOLIDAY_BORDER, "%s%s%1$s");
        add(MekanismLang.HOLIDAY_SIGNATURE, "-aidancbrady");
        add(MekanismLang.CHRISTMAS_LINE_ONE, "Merry Christmas, %s!");
        add(MekanismLang.CHRISTMAS_LINE_TWO, "May you have plenty of Christmas cheer");
        add(MekanismLang.CHRISTMAS_LINE_THREE, "and have a relaxing holiday with your");
        add(MekanismLang.CHRISTMAS_LINE_FOUR, "family :)");
        add(MekanismLang.NEW_YEAR_LINE_ONE, "Happy New Year, %s!");
        add(MekanismLang.NEW_YEAR_LINE_TWO, "Best wishes to you as we enter this");
        add(MekanismLang.NEW_YEAR_LINE_THREE, "new and exciting year of %s! :)");
        //Generic
        add(MekanismLang.GENERIC, "%s");
        add(MekanismLang.GENERIC_STORED, "%s: %s");
        add(MekanismLang.GENERIC_STORED_MB, "%s: %s mB");
        add(MekanismLang.GENERIC_MB, "%s mB");
        add(MekanismLang.GENERIC_PRE_COLON, "%s:");
        add(MekanismLang.GENERIC_SQUARE_BRACKET, "[%s]");
        add(MekanismLang.GENERIC_PARENTHESIS, "(%s)");
        add(MekanismLang.GENERIC_FRACTION, "%s/%s");
        add(MekanismLang.GENERIC_TRANSFER, "- %s (%s)");
        add(MekanismLang.GENERIC_PER_TICK, "%s/t");
        //Hold for
        add(MekanismLang.HOLD_FOR_DETAILS, "Hold %s for details.");
        add(MekanismLang.HOLD_FOR_DESCRIPTION, "Hold %s and %s for a description.");
        //Commands
        add(MekanismLang.COMMAND_CHUNK, "%s chunk %d, %d");
        add(MekanismLang.COMMAND_CHUNK_WATCH, "Chunk %d, %d added to watch list");
        add(MekanismLang.COMMAND_CHUNK_UNWATCH, "Chunk %d, %d removed from watch list");
        add(MekanismLang.COMMAND_CHUNK_CLEAR, "%d chunks removed from watch list");
        add(MekanismLang.COMMAND_CHUNK_FLUSH, "%d chunks unloaded");
        add(MekanismLang.COMMAND_CHUNK_LOADED, "Loaded");
        add(MekanismLang.COMMAND_CHUNK_UNLOADED, "Unloaded");
        add(MekanismLang.COMMAND_DEBUG, "Toggled debug mode: %s.");
        add(MekanismLang.COMMAND_TEST_RULES, "Mob spawning, daylight cycle and weather disabled!");
        add(MekanismLang.COMMAND_TP, "Teleported to %d, %d, %d - saved last position on stack");
        add(MekanismLang.COMMAND_TPOP, "Returned to %d, %d, %d; %d positions on stack");
        add(MekanismLang.COMMAND_TPOP_EMPTY, "No positions on stack");
        //Tooltip stuff
        add(MekanismLang.MODE, "Mode: %s");
        add(MekanismLang.FIRE_MODE, "Fire Mode: %s");
        add(MekanismLang.BUCKET_MODE, "Bucket Mode: %s");
        add(MekanismLang.STORED_ENERGY, "Stored energy: %s");
        add(MekanismLang.STORED, "Stored %s: %s");
        add(MekanismLang.ITEM_AMOUNT, "Item amount: %s");
        add(MekanismLang.FLOWING, "Flowing: %s");
        add(MekanismLang.INVALID, "(Invalid)");
        add(MekanismLang.HAS_INVENTORY, "Inventory: %s");
        add(MekanismLang.NO_GAS, "No gas stored.");
        //Gui stuff
        add(MekanismLang.MIN, "Min: %s");
        add(MekanismLang.MAX, "Max: %s");
        add(MekanismLang.INFINITE, "Infinite");
        add(MekanismLang.NONE, "None");
        add(MekanismLang.EMPTY, "Empty");
        add(MekanismLang.MAX_OUTPUT, "Max Output: %s/t");
        add(MekanismLang.STORING, "Storing: %s");
        add(MekanismLang.DISSIPATED_RATE, "Dissipated: %s/t");
        add(MekanismLang.FUEL, "Fuel: %s");
        add(MekanismLang.VOLUME, "Volume: %s");
        add(MekanismLang.NO_FLUID, "No fluid");
        add(MekanismLang.GAS, "Gas: %s");
        add(MekanismLang.UNIT, "Unit: %s");
        add(MekanismLang.USING, "Using: %s/t");
        add(MekanismLang.NEEDED, "Needed: %s");
        add(MekanismLang.NEEDED_PER_TICK, "Needed: %s/t");
        add(MekanismLang.FINISHED, "Finished: %s");
        add(MekanismLang.NO_RECIPE, "(No recipe)");
        add(MekanismLang.EJECT, "Eject: %s");
        add(MekanismLang.NO_DELAY, "No Delay");
        add(MekanismLang.DELAY, "Delay: %st");
        add(MekanismLang.ENERGY, "Energy: %s");
        add(MekanismLang.RESISTIVE_HEATER_USAGE, "Usage: %s/t");
        add(MekanismLang.DYNAMIC_TANK, "Dynamic Tank");
        add(MekanismLang.CHEMICAL_DISSOLUTION_CHAMBER_SHORT, "C. Dissolution Chamber");
        add(MekanismLang.CHEMICAL_INFUSER_SHORT, "C. Infuser");
        add(MekanismLang.MOVE_UP, "Move Up");
        add(MekanismLang.MOVE_DOWN, "Move Down");
        add(MekanismLang.SET, "Set:");
        //Laser Amplifier
        add(MekanismLang.ENTITY_DETECTION, "Entity Detection");
        add(MekanismLang.ENERGY_CONTENTS, "Energy Contents");
        add(MekanismLang.REDSTONE_OUTPUT, "Redstone Output: %s");
        //Frequency
        add(MekanismLang.FREQUENCY, "Frequency: %s");
        add(MekanismLang.NO_FREQUENCY, "No frequency");
        //Owner
        add(MekanismLang.NOW_OWN, "You now own this item.");
        add(MekanismLang.OWNER, "Owner: %s");
        add(MekanismLang.NO_OWNER, "No Owner");
        //Tab
        add(MekanismLang.MAIN_TAB, "Main");
        //Evaporation
        add(MekanismLang.HEIGHT, "Height: %s");
        add(MekanismLang.FLUID_PRODUCTION, "Production: %s mB/t");
        //Configuration
        add(MekanismLang.TRANSPORTER_CONFIG, "Transporter Config");
        add(MekanismLang.SIDE_CONFIG, "Side Config");
        add(MekanismLang.STRICT_INPUT, "Strict Input");
        add(MekanismLang.STRICT_INPUT_ENABLED, "Strict Input (%s)");
        add(MekanismLang.CONFIG_TYPE, "%s Config");
        add(MekanismLang.NO_EJECT, "Can't Eject");
        add(MekanismLang.SLOTS, "Slots");
        //Auto
        add(MekanismLang.AUTO_PULL, "Auto-pull");
        add(MekanismLang.AUTO_EJECT, "Auto-eject");
        add(MekanismLang.AUTO_SORT, "Auto-sort");
        //Gas mode
        add(MekanismLang.IDLE, "Idle");
        add(MekanismLang.DUMPING_EXCESS, "Dumping Excess");
        add(MekanismLang.DUMPING, "Dumping");
        //Dictionary
        add(MekanismLang.DICTIONARY_KEY, " - %s");
        add(MekanismLang.DICTIONARY_NO_KEY, "No key.");
        add(MekanismLang.DICTIONARY_KEYS_FOUND, "Key(s) found:");
        //Oredictionificator
        add(MekanismLang.LAST_ITEM, "Last Item");
        add(MekanismLang.NEXT_ITEM, "Next Item");
        add(MekanismLang.TAG_COMPAT, "Compatible Tag");
        //Status
        add(MekanismLang.STATUS, "Status: %s");
        add(MekanismLang.STATUS_OK, "All OK");
        //Fluid container
        add(MekanismLang.FLUID_CONTAINER_BOTH, "Both");
        add(MekanismLang.FLUID_CONTAINER_FILL, "Fill");
        add(MekanismLang.FLUID_CONTAINER_EMPTY, "Empty");
        //Boolean values
        add(MekanismLang.YES, "yes");
        add(MekanismLang.NO, "no");
        add(MekanismLang.ON, "on");
        add(MekanismLang.OFF, "off");
        add(MekanismLang.INPUT, "Input");
        add(MekanismLang.OUTPUT, "Output");
        //Capacity
        add(MekanismLang.CAPACITY, "Capacity: %s");
        add(MekanismLang.CAPACITY_ITEMS, "Capacity: %s Items");
        add(MekanismLang.CAPACITY_MB, "Capacity: %s mB");
        add(MekanismLang.CAPACITY_PER_TICK, "Capacity: %s/t");
        add(MekanismLang.CAPACITY_MB_PER_TICK, "Capacity: %s mB/t");
        //Cardboard box
        add(MekanismLang.BLOCK_DATA, "Block data: %s");
        add(MekanismLang.BLOCK, "Block: %s");
        add(MekanismLang.TILE, "Tile: %s");
        //Crafting Formula
        add(MekanismLang.INGREDIENTS, "Ingredients:");
        add(MekanismLang.ENCODED, "(Encoded)");
        //Multiblock
        add(MekanismLang.MULTIBLOCK_INCOMPLETE, "Incomplete");
        add(MekanismLang.MULTIBLOCK_FORMED, "Formed");
        add(MekanismLang.MULTIBLOCK_CONFLICT, "Conflict");
        add(MekanismLang.MULTIBLOCK_FORMED_CHAT, "Multiblock Formed");
        //Transmitter tooltips
        add(MekanismLang.UNIVERSAL, "universal");
        add(MekanismLang.ITEMS, "- Items (%s)");
        add(MekanismLang.BLOCKS, "- Blocks (%s)");
        add(MekanismLang.FLUIDS, "- Fluids (%s)");
        add(MekanismLang.GASES, "- Gases (%s)");
        add(MekanismLang.HEAT, "- Heat (%s)");
        add(MekanismLang.CONDUCTION, "Conduction: %s");
        add(MekanismLang.INSULATION, "Insulation: %s");
        add(MekanismLang.HEAT_CAPACITY, "Heat Capacity: %s");
        add(MekanismLang.CAPABLE_OF_TRANSFERRING, "Capable of transferring:");
        add(MekanismLang.DIVERSION_CONTROL_DISABLED, "Always active");
        add(MekanismLang.DIVERSION_CONTROL_HIGH, "Active with signal");
        add(MekanismLang.DIVERSION_CONTROL_LOW, "Active without signal");
        add(MekanismLang.TOGGLE_DIVERTER, "Diverter mode changed to: %s");
        add(MekanismLang.PUMP_RATE, "Pump Rate: %s/s");
        add(MekanismLang.PUMP_RATE_MB, "Pump Rate: %s mB/s");
        add(MekanismLang.SPEED, "Speed: %s m/s");
        //Condensentrator
        add(MekanismLang.CONDENSENTRATOR_TOGGLE, "Toggle operation");
        add(MekanismLang.CONDENSENTRATING, "Condensentrating");
        add(MekanismLang.DECONDENSENTRATING, "Decondensentrating");
        //Upgrades
        add(MekanismLang.UPGRADE_DISPLAY, "- %s");
        add(MekanismLang.UPGRADE_DISPLAY_LEVEL, "- %s: x%s");
        add(MekanismLang.UPGRADES_EFFECT, "Effect: %sx");
        add(MekanismLang.UPGRADES, "Upgrades");
        add(MekanismLang.UPGRADE_NO_SELECTION, "No selection.");
        add(MekanismLang.UPGRADES_SUPPORTED, "Supported:");
        add(MekanismLang.UPGRADE_COUNT, "Amount: %s/%s");
        add(MekanismLang.UPGRADE_TYPE, "%s Upgrade");
        //Filter
        add(MekanismLang.CREATE_FILTER_TITLE, "Create New Filter");
        add(MekanismLang.FILTERS, "Filters:");
        add(MekanismLang.FILTER_COUNT, "T: %s");
        add(MekanismLang.FILTER_ALLOW_DEFAULT, "Allow Default");
        add(MekanismLang.FILTER, "Filter");
        add(MekanismLang.FILTER_NEW, "New: %s");
        add(MekanismLang.FILTER_EDIT, "Edit: %s");
        add(MekanismLang.FILTER_INDEX, "Index: %s");
        add(MekanismLang.SIZE_MODE, "Size Mode");
        add(MekanismLang.SIZE_MODE_CONFLICT, "Size Mode - has no effect currently, because single item mode is turned on.");
        add(MekanismLang.MATERIAL_FILTER, "Material Filter");
        add(MekanismLang.MATERIAL_FILTER_DETAILS, "Using material of:");
        add(MekanismLang.TAG_FILTER, "Tag Filter");
        add(MekanismLang.TAG_FILTER_NO_TAG, "No tag");
        add(MekanismLang.TAG_FILTER_SAME_TAG, "Same tag");
        add(MekanismLang.TAG_FILTER_TAG, "Tag: %s");
        add(MekanismLang.MODID_FILTER, "Mod ID Filter");
        add(MekanismLang.MODID_FILTER_NO_ID, "No ID");
        add(MekanismLang.MODID_FILTER_SAME_ID, "Same ID");
        add(MekanismLang.MODID_FILTER_ID, "ID: %s");
        add(MekanismLang.ITEM_FILTER, "Item Filter");
        add(MekanismLang.ITEM_FILTER_NO_ITEM, "No item");
        add(MekanismLang.ITEM_FILTER_SIZE_MODE, "%s!");
        add(MekanismLang.ITEM_FILTER_DETAILS, "ItemStack Details:");
        add(MekanismLang.ITEM_FILTER_MAX_LESS_THAN_MIN, "Max < min");
        add(MekanismLang.ITEM_FILTER_OVER_SIZED, "Max > 64");
        add(MekanismLang.ITEM_FILTER_SIZE_MISSING, "Max/min");
        //Seismic Vibrator
        add(MekanismLang.CHUNK, "Chunk: %s, %s");
        add(MekanismLang.VIBRATING, "Vibrating");
        //Seismic Reader
        add(MekanismLang.NEEDS_ENERGY, "Not enough energy to interpret vibration");
        add(MekanismLang.NO_VIBRATIONS, "Unable to discover any vibrations");
        add(MekanismLang.ABUNDANCY, "Abundancy: %s");
        //Redstone Control
        add(MekanismLang.REDSTONE_CONTROL_DISABLED, "Disabled");
        add(MekanismLang.REDSTONE_CONTROL_HIGH, "High");
        add(MekanismLang.REDSTONE_CONTROL_LOW, "Low");
        add(MekanismLang.REDSTONE_CONTROL_PULSE, "Pulse");
        //Security
        add(MekanismLang.SECURITY, "Security: %s");
        add(MekanismLang.SECURITY_OVERRIDDEN, "(Overridden)");
        add(MekanismLang.SECURITY_OFFLINE, "Security Offline");
        add(MekanismLang.SECURITY_ADD, "Add:");
        add(MekanismLang.SECURITY_OVERRIDE, "Security Override: %s");
        add(MekanismLang.NO_ACCESS, "You don't have access.");
        add(MekanismLang.TRUSTED_PLAYERS, "Trusted Players");
        add(MekanismLang.PUBLIC, "Public");
        add(MekanismLang.TRUSTED, "Trusted");
        add(MekanismLang.PRIVATE, "Private");
        add(MekanismLang.PUBLIC_MODE, "Public Mode");
        add(MekanismLang.TRUSTED_MODE, "Trusted Mode");
        add(MekanismLang.PRIVATE_MODE, "Private Mode");
        //Formulaic Assemblicator
        add(MekanismLang.ENCODE_FORMULA, "Encode Formula");
        add(MekanismLang.CRAFT_SINGLE, "Craft Single Item");
        add(MekanismLang.CRAFT_AVAILABLE, "Craft Available Items");
        add(MekanismLang.FILL_EMPTY, "Fill/Empty Grid");
        add(MekanismLang.STOCK_CONTROL, "Stock Control: %s");
        add(MekanismLang.AUTO_MODE, "Auto-Mode: %s");
        //Factory Type
        add(MekanismLang.FACTORY_TYPE, "Recipe type: %s");
        add(MekanismLang.SMELTING, "Smelting");
        add(MekanismLang.ENRICHING, "Enriching");
        add(MekanismLang.CRUSHING, "Crushing");
        add(MekanismLang.COMPRESSING, "Compressing");
        add(MekanismLang.COMBINING, "Combining");
        add(MekanismLang.PURIFYING, "Purifying");
        add(MekanismLang.INJECTING, "Injecting");
        add(MekanismLang.INFUSING, "Infusing");
        add(MekanismLang.SAWING, "Sawing");
        //Transmitter Networks
        add(MekanismLang.NETWORK_DESCRIPTION, "[%s] %s transmitters, %s acceptors.");
        add(MekanismLang.INVENTORY_NETWORK, "InventoryNetwork");
        add(MekanismLang.FLUID_NETWORK, "FluidNetwork");
        add(MekanismLang.GAS_NETWORK, "GasNetwork");
        add(MekanismLang.HEAT_NETWORK, "HeatNetwork");
        add(MekanismLang.ENERGY_NETWORK, "EnergyNetwork");
        add(MekanismLang.NO_NETWORK, "No Network");
        add(MekanismLang.NOT_APPLICABLE, "Not Applicable");
        add(MekanismLang.HEAT_NETWORK_STORED, "%s above ambient");
        add(MekanismLang.HEAT_NETWORK_FLOW, "%s transferred to acceptors, %s lost to environment.");
        add(MekanismLang.HEAT_NETWORK_FLOW_EFFICIENCY, "%s transferred to acceptors, %s lost to environment, %s% efficiency.");
        add(MekanismLang.FLUID_NETWORK_NEEDED, "%s buckets");
        add(MekanismLang.NETWORK_MB_PER_TICK, "%s mB/t");
        add(MekanismLang.NETWORK_MB_STORED, "%s (%s mB)");
        //Button
        add(MekanismLang.BUTTON_CONFIRM, "Confirm");
        add(MekanismLang.BUTTON_START, "Start");
        add(MekanismLang.BUTTON_STOP, "Stop");
        add(MekanismLang.BUTTON_CONFIG, "Config");
        add(MekanismLang.BUTTON_REMOVE, "Remove");
        add(MekanismLang.BUTTON_SAVE, "Save");
        add(MekanismLang.BUTTON_SET, "Set");
        add(MekanismLang.BUTTON_DELETE, "Delete");
        add(MekanismLang.BUTTON_TELEPORT, "Teleport");
        add(MekanismLang.BUTTON_NEW_FILTER, "New Filter");
        add(MekanismLang.BUTTON_ITEMSTACK_FILTER, "ItemStack");
        add(MekanismLang.BUTTON_TAG_FILTER, "Tag");
        add(MekanismLang.BUTTON_MATERIAL_FILTER, "Material");
        add(MekanismLang.BUTTON_MODID_FILTER, "Mod ID");
        //Configuration Card
        add(MekanismLang.CONFIG_CARD_GOT, "Retrieved configuration data from %s");
        add(MekanismLang.CONFIG_CARD_SET, "Injected configuration data of type %s");
        add(MekanismLang.CONFIG_CARD_UNEQUAL, "Unequal configuration data formats.");
        add(MekanismLang.CONFIG_CARD_HAS_DATA, "Data: %s");
        //Connection Type
        add(MekanismLang.CONNECTION_NORMAL, "Normal");
        add(MekanismLang.CONNECTION_PUSH, "Push");
        add(MekanismLang.CONNECTION_PULL, "Pull");
        add(MekanismLang.CONNECTION_NONE, "None");
        //Teleporter
        add(MekanismLang.TELEPORTER_READY, "Ready");
        add(MekanismLang.TELEPORTER_NO_FRAME, "No frame");
        add(MekanismLang.TELEPORTER_NO_LINK, "No link");
        add(MekanismLang.TELEPORTER_NEEDS_ENERGY, "Needs energy");
        //Matrix
        add(MekanismLang.MATRIX, "Induction Matrix");
        add(MekanismLang.MATRIX_RECEIVING_RATE, "Receiving: %s/t");
        add(MekanismLang.MATRIX_OUTPUT_AMOUNT, "Output:");
        add(MekanismLang.MATRIX_OUTPUT_RATE, "Output: %s/t");
        add(MekanismLang.MATRIX_OUTPUTTING_RATE, "Outputting: %s/t");
        add(MekanismLang.MATRIX_INPUT_AMOUNT, "Input:");
        add(MekanismLang.MATRIX_INPUT_RATE, "Input: %s/t");
        add(MekanismLang.MATRIX_CONSTITUENTS, "Constituents:");
        add(MekanismLang.MATRIX_DIMENSIONS, "Dimensions:");
        add(MekanismLang.MATRIX_DIMENSION_REPRESENTATION, "%s x %s x %s");
        add(MekanismLang.MATRIX_STATS, "Matrix Statistics");
        add(MekanismLang.MATRIX_CELLS, "%s cells");
        add(MekanismLang.MATRIX_PROVIDERS, "%s providers");
        add(MekanismLang.INDUCTION_PORT_MODE, "Toggled Induction Port transfer mode to %s.");
        add(MekanismLang.INDUCTION_PORT_OUTPUT_RATE, "Output Rate: %s");
        //Miner
        add(MekanismLang.MINER_INSUFFICIENT_BUFFER, "Insufficient energy buffer!");
        add(MekanismLang.MINER_BUFFER_FREE, "Free Buffer: %s");
        add(MekanismLang.MINER_TO_MINE, "To mine: %s");
        add(MekanismLang.MINER_SILK_ENABLED, "Silk: %s");
        add(MekanismLang.MINER_AUTO_PULL, "Pull: %s");
        add(MekanismLang.MINER_RUNNING, "Running");
        add(MekanismLang.MINER_LOW_POWER, "Low Power");
        add(MekanismLang.MINER_ENERGY_CAPACITY, "Energy Capacity: %s");
        add(MekanismLang.MINER_MISSING_BLOCK, "Missing block");
        add(MekanismLang.MINER_WELL, "All is well!");
        add(MekanismLang.MINER_CONFIG, "Digital Miner Config");
        add(MekanismLang.MINER_SILK, "Silk touch");
        add(MekanismLang.MINER_RESET, "Reset");
        add(MekanismLang.MINER_INVERSE, "Inverse mode");
        add(MekanismLang.MINER_VISUALS, "Visuals: %s");
        add(MekanismLang.MINER_VISUALS_TOO_BIG, "Radius too big to display visuals");
        add(MekanismLang.MINER_FUZZY_MODE, "Fuzzy mode: %s");
        add(MekanismLang.MINER_REQUIRE_REPLACE, "Require replace: %s");
        add(MekanismLang.MINER_IS_INVERSE, "I: %s");
        add(MekanismLang.MINER_RADIUS, "Radi: %s");
        add(MekanismLang.MINER_IDLE, "Not ready");
        add(MekanismLang.MINER_SEARCHING, "Searching");
        add(MekanismLang.MINER_PAUSED, "Paused");
        add(MekanismLang.MINER_READY, "Ready");
        //Boiler
        add(MekanismLang.BOILER, "Thermoelectric Boiler");
        add(MekanismLang.BOILER_STATS, "Boiler Statistics");
        add(MekanismLang.BOILER_MAX_WATER, "Max Water: %s mB");
        add(MekanismLang.BOILER_MAX_STEAM, "Max Steam: %s mB");
        add(MekanismLang.BOILER_HEAT_TRANSFER, "Heat Transfer");
        add(MekanismLang.BOILER_HEATERS, "Superheaters: %s");
        add(MekanismLang.BOILER_CAPACITY, "Boil Capacity: %s mB/t");
        add(MekanismLang.BOIL_RATE, "Boil Rate: %s mB/t");
        add(MekanismLang.MAX_BOIL_RATE, "Max Boil: %s mB/t");
        //Temperature
        add(MekanismLang.TEMPERATURE, "Temp: %s");
        add(MekanismLang.TEMPERATURE_KELVIN, "Kelvin");
        add(MekanismLang.TEMPERATURE_KELVIN_SHORT, "K");
        add(MekanismLang.TEMPERATURE_CELSIUS, "Celsius");
        add(MekanismLang.TEMPERATURE_CELSIUS_SHORT, "C");
        add(MekanismLang.TEMPERATURE_RANKINE, "Rankine");
        add(MekanismLang.TEMPERATURE_RANKINE_SHORT, "R");
        add(MekanismLang.TEMPERATURE_FAHRENHEIT, "Fahrenheit");
        add(MekanismLang.TEMPERATURE_FAHRENHEIT_SHORT, "F");
        add(MekanismLang.TEMPERATURE_AMBIENT, "Ambient");
        add(MekanismLang.TEMPERATURE_AMBIENT_SHORT, "STP");
        //Energy
        add(MekanismLang.ENERGY_JOULES, "Joule");
        add(MekanismLang.ENERGY_JOULES_PLURAL, "Joules");
        add(MekanismLang.ENERGY_JOULES_SHORT, "J");
        add(MekanismLang.ENERGY_FORGE, "Forge Energy");
        add(MekanismLang.ENERGY_FORGE_SHORT, "FE");
        add(MekanismLang.ENERGY_EU, "Electrical Unit");
        add(MekanismLang.ENERGY_EU_PLURAL, "Electrical Units");
        add(MekanismLang.ENERGY_EU_SHORT, "EU");
        //Network Reader
        add(MekanismLang.NETWORK_READER_BORDER, "%s %s %1$s");
        add(MekanismLang.NETWORK_READER_ABOVE_AMBIENT, " *Temperature: %sK above ambient.");
        add(MekanismLang.NETWORK_READER_TRANSMITTERS, " *Transmitters: %s");
        add(MekanismLang.NETWORK_READER_ACCEPTORS, " *Acceptors: %s");
        add(MekanismLang.NETWORK_READER_NEEDED, " *Needed: %s");
        add(MekanismLang.NETWORK_READER_BUFFER, " *Buffer: %s");
        add(MekanismLang.NETWORK_READER_THROUGHPUT, " *Throughput: %s");
        add(MekanismLang.NETWORK_READER_CAPACITY, " *Capacity: %s");
        add(MekanismLang.NETWORK_READER_CONNECTED_SIDES, " *Connected sides: %s");
        //Sorter
        add(MekanismLang.SORTER_DEFAULT, "Default:");
        add(MekanismLang.SORTER_SINGLE_ITEM, "Single:");
        add(MekanismLang.SORTER_ROUND_ROBIN, "RR:");
        add(MekanismLang.SORTER_AUTO_EJECT, "Auto:");
        add(MekanismLang.SORTER_SINGLE_ITEM_DESCRIPTION, "Sends a single item instead of a whole stack each time (overrides min and max set in ItemStack filters).");
        add(MekanismLang.SORTER_ROUND_ROBIN_DESCRIPTION, "Cycles between all connected inventories when sending items.");
        add(MekanismLang.SORTER_AUTO_EJECT_DESCRIPTION, "Ejects items automatically to connected inventories.");
        //Side data/config
        add(MekanismLang.SIDE_DATA_NONE, "None");
        add(MekanismLang.SIDE_DATA_INPUT, "Input");
        add(MekanismLang.SIDE_DATA_OUTPUT, "Output");
        add(MekanismLang.SIDE_DATA_ENERGY, "Energy");
        add(MekanismLang.SIDE_DATA_EXTRA, "Extra");
        //Free runner modes
        add(MekanismLang.FREE_RUNNER_NORMAL, "Regular");
        add(MekanismLang.FREE_RUNNER_DISABLED, "Disabled");
        //Jetpack Modes
        add(MekanismLang.JETPACK_NORMAL, "Regular");
        add(MekanismLang.JETPACK_HOVER, "Hover");
        add(MekanismLang.JETPACK_DISABLED, "Disabled");
        //Disassembler Mode
        add(MekanismLang.DISASSEMBLER_MODE_TOGGLE, "Mode toggled to: %s %s");
        add(MekanismLang.DISASSEMBLER_EFFICIENCY, "Efficiency: %s");
        add(MekanismLang.DISASSEMBLER_NORMAL, "normal");
        add(MekanismLang.DISASSEMBLER_SLOW, "slow");
        add(MekanismLang.DISASSEMBLER_FAST, "fast");
        add(MekanismLang.DISASSEMBLER_VEIN, "vein");
        add(MekanismLang.DISASSEMBLER_EXTENDED_VEIN, "extended vein mining");
        add(MekanismLang.DISASSEMBLER_OFF, "off");
        //Flamethrower Modes
        add(MekanismLang.FLAMETHROWER_MODE_BUMP, "Flamethrower mode bumped to %s");
        add(MekanismLang.FLAMETHROWER_COMBAT, "Combat");
        add(MekanismLang.FLAMETHROWER_HEAT, "Heat");
        add(MekanismLang.FLAMETHROWER_INFERNO, "Inferno");
        //Configurator
        add(MekanismLang.CONFIGURE_STATE, "Configure State: %s");
        add(MekanismLang.STATE, "State: %s");
        add(MekanismLang.TOGGLE_COLOR, "Color bumped to: %s");
        add(MekanismLang.CURRENT_COLOR, "Current color: %s");
        add(MekanismLang.PUMP_RESET, "Reset Electric Pump calculation");
        add(MekanismLang.PLENISHER_RESET, "Reset Fluidic Plenisher calculation");
        add(MekanismLang.REDSTONE_SENSITIVITY, "Redstone sensitivity turned: %s");
        add(MekanismLang.CONNECTION_TYPE, "Connection type changed to: %s");
        //Configurator Modes
        add(MekanismLang.CONFIGURATOR_VIEW_MODE, "Current %s behavior: %s (%s)");
        add(MekanismLang.CONFIGURATOR_TOGGLE_MODE, "%s behavior bumped to: %s (%s)");
        add(MekanismLang.CONFIGURATOR_CONFIGURATE, "Configurate (%s)");
        add(MekanismLang.CONFIGURATOR_EMPTY, "Empty");
        add(MekanismLang.CONFIGURATOR_ROTATE, "Rotate");
        add(MekanismLang.CONFIGURATOR_WRENCH, "Wrench");
        //Robit
        add(MekanismLang.ROBIT, "Robit");
        add(MekanismLang.ROBIT_NAME, "Name: %s");
        add(MekanismLang.ROBIT_SMELTING, "Robit Smelting");
        add(MekanismLang.ROBIT_CRAFTING, "Robit Crafting");
        add(MekanismLang.ROBIT_INVENTORY, "Robit Inventory");
        add(MekanismLang.ROBIT_REPAIR, "Robit Repair");
        add(MekanismLang.ROBIT_TELEPORT, "Teleport back home");
        add(MekanismLang.ROBIT_TOGGLE_PICKUP, "Toggle 'drop pickup' mode");
        add(MekanismLang.ROBIT_RENAME, "Rename this Robit");
        add(MekanismLang.ROBIT_TOGGLE_FOLLOW, "Toggle 'follow' mode");
        add(MekanismLang.ROBIT_GREETING, "Hi, I'm %s!");
        add(MekanismLang.ROBIT_OWNER, "Owner: %s");
        add(MekanismLang.ROBIT_FOLLOWING, "Following: %s");
        add(MekanismLang.ROBIT_DROP_PICKUP, "Drop pickup: %s");
        //Descriptions
        add(MekanismLang.DESCRIPTION_SEISMIC_READER, "A portable machine that uses seismic vibrations to provide information on differing layers of the world.");
        add(MekanismLang.DESCRIPTION_BIN, "A block used to store large quantities of a single type of item.");
        add(MekanismLang.DESCRIPTION_TELEPORTER_FRAME, "The frame used to construct the Teleporter multiblock, allowing a portal to be generated within the structure.");
        add(MekanismLang.DESCRIPTION_STEEL_CASING, "A sturdy, steel-based casing used as a foundation for machinery.");
        add(MekanismLang.DESCRIPTION_DYNAMIC_TANK, "The casing used in the Dynamic Tank multiblock, a structure capable of storing great amounts of fluid.");
        add(MekanismLang.DESCRIPTION_STRUCTURAL_GLASS, "An advanced, reinforced material of glass that drops when broken and can be used in the structure of any applicable multiblock.");
        add(MekanismLang.DESCRIPTION_DYNAMIC_VALVE, "A valve that can be placed on a Dynamic Tank multiblock, allowing for fluids to be inserted and extracted via external piping.");
        add(MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_CONTROLLER, "The controller for a Thermal Evaporation Plant, acting as the master block of the structure. Only one of these should be placed on a multiblock.");
        add(MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_VALVE, "A valve that can be placed on a Thermal Evaporation Plant multiblock, allowing for fluids to be inserted and extracted via external piping.");
        add(MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_BLOCK, "A copper-alloyed casing used in the structure of a Thermal Evaporation Plant, using its advanced material to conduct the great amounts of heat necessary for processing.");
        add(MekanismLang.DESCRIPTION_INDUCTION_CASING, "A type of energy-resistant casing used in the creation of an Energized Induction Matrix multiblock.");
        add(MekanismLang.DESCRIPTION_INDUCTION_PORT, "A port that can be placed on an Energized Induction Matrix multiblock, allowing for energy to be inserted from and output to external cabling.");
        add(MekanismLang.DESCRIPTION_INDUCTION_CELL, "A highly conductive energy capacitor capable of storing massive amounts of energy in a single block. Housed in an Energized Induction Matrix to expand the multiblock's energy storage.");
        add(MekanismLang.DESCRIPTION_INDUCTION_PROVIDER, "An advanced complex of coolant systems, conductors and transformers capable of expanding the Energized Induction Matrix's maximum rate of energy transfer.");
        add(MekanismLang.DESCRIPTION_SUPERHEATING_ELEMENT, "A modular, somewhat dangerous radiator that is capable of emitting massive amounts of heat to its surroundings.");
        add(MekanismLang.DESCRIPTION_PRESSURE_DISPERSER, "A block used disperse steam throughout a multiblock structure. These should form a gapless, horizontal plane in order to properly control steam flow.");
        add(MekanismLang.DESCRIPTION_BOILER_CASING, "A pressure-resistant, dense casing used in the creation of a Thermoelectric Boiler multiblock.");
        add(MekanismLang.DESCRIPTION_BOILER_VALVE, "A valve that can be placed on a Thermoelectric Boiler multiblock, allowing for the insertion of energy and water along with the extraction of produced steam.");
        add(MekanismLang.DESCRIPTION_SECURITY_DESK, "A central control hub for managing the security of all your owned machinery.");
        add(MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, "A simple machine used to enrich ores into two of their dust counterparts, as well as perform many other operations.");
        add(MekanismLang.DESCRIPTION_OSMIUM_COMPRESSOR, "A fairly advanced machine used to compress osmium into various dusts in order to create their ingot counterparts.");
        add(MekanismLang.DESCRIPTION_COMBINER, "A machine used to combine dusts and cobblestone to form their ore counterparts.");
        add(MekanismLang.DESCRIPTION_CRUSHER, "A machine used to crush ingots into their dust counterparts, as well as perform many other operations.");
        add(MekanismLang.DESCRIPTION_DIGITAL_MINER, "A highly-advanced, filter-based, auto-miner that can mine whatever block you tell it to within a 32 block (max) radius.");
        add(MekanismLang.DESCRIPTION_METALLURGIC_INFUSER, "A machine used to infuse various materials into (generally) metals to create metal alloys and other compounds.");
        add(MekanismLang.DESCRIPTION_PURIFICATION_CHAMBER, "An advanced machine capable of processing ores into three clumps, serving as the initial stage of 300% ore processing.");
        add(MekanismLang.DESCRIPTION_ENERGIZED_SMELTER, "A simple machine that serves as a Mekanism-based furnace that runs off of energy.");
        add(MekanismLang.DESCRIPTION_TELEPORTER, "A machine capable of teleporting players to various locations defined by another teleporter.");
        add(MekanismLang.DESCRIPTION_ELECTRIC_PUMP, "An advanced, upgradeable pump, capable of extracting any type of fluid.");
        add(MekanismLang.DESCRIPTION_PERSONAL_CHEST, "A 54-slot chest that can be opened anywhere- even from your own inventory.");
        add(MekanismLang.DESCRIPTION_CHARGEPAD, "A universal chargepad that can charge any energized item from any mod.");
        add(MekanismLang.DESCRIPTION_LOGISTICAL_SORTER, "A filter-based, advanced sorting machine that can auto-eject specified items out of and into adjacent inventories and Logistical Transporters.");
        add(MekanismLang.DESCRIPTION_ROTARY_CONDENSENTRATOR, "A machine capable of converting gases into their fluid form and vice versa.");
        add(MekanismLang.DESCRIPTION_CHEMICAL_INJECTION_CHAMBER, "An elite machine capable of processing ores into four shards, serving as the initial stage of 400% ore processing.");
        add(MekanismLang.DESCRIPTION_ELECTROLYTIC_SEPARATOR, "A machine that uses the process of electrolysis to split apart a certain gas into two different gases.");
        add(MekanismLang.DESCRIPTION_PRECISION_SAWMILL, "A machine used to process logs and other wood-based items more efficiently, as well as to obtain sawdust.");
        add(MekanismLang.DESCRIPTION_CHEMICAL_DISSOLUTION_CHAMBER, "An ultimate machine used to chemically dissolve all impurities of an ore, leaving an unprocessed slurry behind.");
        add(MekanismLang.DESCRIPTION_CHEMICAL_WASHER, "An ultimate machine that cleans unprocessed slurry and prepares it for crystallization.");
        add(MekanismLang.DESCRIPTION_CHEMICAL_CRYSTALLIZER, "An ultimate machine used to crystallize purified ore slurry into ore crystals.");
        add(MekanismLang.DESCRIPTION_CHEMICAL_OXIDIZER, "A machine capable of oxidizing solid materials into gas phase.");
        add(MekanismLang.DESCRIPTION_CHEMICAL_INFUSER, "A machine that produces a new gas by infusing two others.");
        add(MekanismLang.DESCRIPTION_SEISMIC_VIBRATOR, "A machine that uses seismic vibrations to provide information on differing layers of the world.");
        add(MekanismLang.DESCRIPTION_PRESSURIZED_REACTION_CHAMBER, "An advanced machine that processes a solid, liquid and gaseous mixture and creates both a gaseous and solid product.");
        add(MekanismLang.DESCRIPTION_FLUID_TANK, "A handy, sturdy, portable tank that lets you carry multiple buckets of fluid wherever you please. Also doubles as a bucket!");
        add(MekanismLang.DESCRIPTION_FLUIDIC_PLENISHER, "A machine that is capable of creating entire lakes by filling ravines with fluids.");
        add(MekanismLang.DESCRIPTION_LASER, "An advanced form of linear energy transfer that utilizes an extremely collimated beam of light.");
        add(MekanismLang.DESCRIPTION_LASER_AMPLIFIER, "A block that can be used to merge, redirect and amplify laser beams, with fine controls over when to fire.");
        add(MekanismLang.DESCRIPTION_LASER_TRACTOR_BEAM, "A block used to merge and redirect laser beams. Collects drops from blocks it has broken.");
        add(MekanismLang.DESCRIPTION_SOLAR_NEUTRON_ACTIVATOR, "A machine that directs the neutron radiation of the sun into its internal reservoir, allowing for the slow creation of various isotopes.");
        add(MekanismLang.DESCRIPTION_OREDICTIONIFICATOR, "A machine used to unify and translate between various items and blocks using the Ore Dictionary.");
        add(MekanismLang.DESCRIPTION_FACTORY, "A machine that serves as an upgrade to regular machinery, allowing for multiple processing operations to occur at once.");
        add(MekanismLang.DESCRIPTION_RESISTIVE_HEATER, "A condensed, coiled resistor capable of converting electrical energy directly into heat energy.");
        add(MekanismLang.DESCRIPTION_FORMULAIC_ASSEMBLICATOR, "A machine that uses energy to rapidly craft items and blocks from Crafting Formulas. Doubles as an advanced crafting bench.");
        add(MekanismLang.DESCRIPTION_FUELWOOD_HEATER, "A machine that is capable of producing large quantities of heat energy by burning combustible items.");
        add(MekanismLang.DESCRIPTION_QUANTUM_ENTANGLOPORTER, "A highly-advanced block capable of transmitting any practical resource across long distances and dimensions.");
        add(MekanismLang.DESCRIPTION_ENERGY_CUBE, "An advanced device for storing and distributing energy.");
        add(MekanismLang.DESCRIPTION_GAS_TANK, "A portable tank that lets you carry gas wherever you please.");
        add(MekanismLang.DESCRIPTION_DIVERSION, "- Controllable by redstone");
        add(MekanismLang.DESCRIPTION_RESTRICTIVE, "- Only used if no other paths available");
        add(MekanismLang.DESCRIPTION_OSMIUM_ORE, "A strong mineral that can be found at nearly any height in the world. It is known to have many uses in the construction of machinery.");
        add(MekanismLang.DESCRIPTION_COPPER_ORE, "A common, conductive material that can be used in the production of wires. Its ability to withstand high heats also makes it essential to advanced machinery.");
        add(MekanismLang.DESCRIPTION_TIN_ORE, "A lightweight, yet sturdy, conductive material that is found slightly less commonly than Copper.");
    }

    private void addOreProcessingNames(IItemProvider crystal, IItemProvider shard, IItemProvider clump, IItemProvider dirtyDust, IItemProvider dust, String resourceName) {
        add(crystal, resourceName + " Crystal");
        add(shard, resourceName + " Shard");
        add(clump, resourceName + " Clump");
        add(dirtyDust, "Dirty " + resourceName + " Dust");
        add(dust, resourceName + " Dust");
    }

    private void addTiered(IItemProvider basic, IItemProvider advanced, IItemProvider elite, IItemProvider ultimate, String name) {
        add(basic, "Basic " + name);
        add(advanced, "Advanced " + name);
        add(elite, "Elite " + name);
        add(ultimate, "Ultimate " + name);
    }

    private void addTiered(IItemProvider basic, IItemProvider advanced, IItemProvider elite, IItemProvider ultimate, IItemProvider creative, String name) {
        addTiered(basic, advanced, elite, ultimate, name);
        add(creative, "Creative " + name);
    }

    private void addFluid(FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> fluidRO, String name) {
        add(fluidRO.getStillFluid().getAttributes().getTranslationKey(), name);
        add(fluidRO.getFlowingFluid().getAttributes().getTranslationKey(), "Flowing " + name);
        add(fluidRO.getBlock(), name);
        add(fluidRO.getBucket(), name + " Bucket");
    }

    private void addSlurry(SlurryRegistryObject<Slurry, Slurry> slurryRO, String name) {
        add(slurryRO.getDirtySlurry(), name);
        add(slurryRO.getCleanSlurry(), "Clean " + name);
    }
}