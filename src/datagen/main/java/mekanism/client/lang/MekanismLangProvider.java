package mekanism.client.lang;

import com.google.common.collect.Table.Cell;
import java.util.Locale;
import java.util.Map;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDamageSource;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismSlurries;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.resource.OreType;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;

public class MekanismLangProvider extends BaseLanguageProvider {

    public MekanismLangProvider(DataGenerator gen) {
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
        addPigments();
        addSlurries();
        addDamageSources();
        addSubtitles();
        addMisc();
    }

    private void addItems() {
        add(MekanismItems.ROBIT, "Robit");
        add(MekanismItems.ENERGY_TABLET, "Energy Tablet");
        add(MekanismItems.CONFIGURATION_CARD, "Configuration Card");
        add(MekanismItems.CRAFTING_FORMULA, "Crafting Formula");
        add(MekanismItems.TELEPORTATION_CORE, "Teleportation Core");
        add(MekanismItems.ENRICHED_IRON, "Enriched Iron");
        add(MekanismItems.ELECTROLYTIC_CORE, "Electrolytic Core");
        add(MekanismItems.SAWDUST, "Sawdust");
        add(MekanismItems.SALT, "Salt");
        add(MekanismItems.SUBSTRATE, "Substrate");
        add(MekanismItems.BIO_FUEL, "Bio Fuel");
        add(MekanismItems.FLUORITE_GEM, "Fluorite");
        add(MekanismItems.YELLOW_CAKE_URANIUM, "Yellow Cake Uranium");
        add(MekanismItems.ANTIMATTER_PELLET, "Antimatter Pellet");
        add(MekanismItems.PLUTONIUM_PELLET, "Plutonium Pellet");
        add(MekanismItems.POLONIUM_PELLET, "Polonium Pellet");
        add(MekanismItems.REPROCESSED_FISSILE_FRAGMENT, "Reprocessed Fissile Fragment");
        add(MekanismItems.MODULE_BASE, "Module Base");
        add(MekanismItems.PORTABLE_QIO_DASHBOARD, "Portable QIO Dashboard");
        //Tools/Armor
        add(MekanismItems.GAUGE_DROPPER, "Gauge Dropper");
        add(MekanismItems.DICTIONARY, "Dictionary");
        add(MekanismItems.CONFIGURATOR, "Configurator");
        add(MekanismItems.NETWORK_READER, "Network Reader");
        add(MekanismItems.SEISMIC_READER, "Seismic Reader");
        add(MekanismItems.PORTABLE_TELEPORTER, "Portable Teleporter");
        add(MekanismItems.ELECTRIC_BOW, "Electric Bow");
        add(MekanismItems.ATOMIC_DISASSEMBLER, "Atomic Disassembler");
        add(MekanismItems.SCUBA_MASK, "Scuba Mask");
        add(MekanismItems.SCUBA_TANK, "Scuba Tank");
        add(MekanismItems.FLAMETHROWER, "Flamethrower");
        add(MekanismItems.FREE_RUNNERS, "Free Runners");
        add(MekanismItems.JETPACK, "Jetpack");
        add(MekanismItems.ARMORED_JETPACK, "Armored Jetpack");
        add(MekanismItems.GEIGER_COUNTER, "Geiger Counter");
        add(MekanismItems.DOSIMETER, "Dosimeter");
        add(MekanismItems.CANTEEN, "Canteen");
        add(MekanismItems.MEKA_TOOL, "Meka-Tool");
        add(MekanismItems.HAZMAT_MASK, "Hazmat Mask");
        add(MekanismItems.HAZMAT_GOWN, "Hazmat Gown");
        add(MekanismItems.HAZMAT_PANTS, "Hazmat Pants");
        add(MekanismItems.HAZMAT_BOOTS, "Hazmat Boots");
        add(MekanismItems.MEKASUIT_HELMET, "MekaSuit Helmet");
        add(MekanismItems.MEKASUIT_BODYARMOR, "MekaSuit Bodyarmor");
        add(MekanismItems.MEKASUIT_PANTS, "MekaSuit Pants");
        add(MekanismItems.MEKASUIT_BOOTS, "MekaSuit Boots");
        //Drives
        add(MekanismItems.BASE_QIO_DRIVE, "QIO Drive");
        add(MekanismItems.HYPER_DENSE_QIO_DRIVE, "Hyper-Dense QIO Drive");
        add(MekanismItems.TIME_DILATING_QIO_DRIVE, "Time-Dilating QIO Drive");
        add(MekanismItems.SUPERMASSIVE_QIO_DRIVE, "Supermassive QIO Drive");
        //HDPE
        add(MekanismItems.HDPE_PELLET, "HDPE Pellet");
        add(MekanismItems.HDPE_ROD, "HDPE Rod");
        add(MekanismItems.HDPE_SHEET, "HDPE Sheet");
        add(MekanismItems.HDPE_STICK, "PlaStick");
        //Enriched Items
        add(MekanismItems.ENRICHED_CARBON, "Enriched Carbon");
        add(MekanismItems.ENRICHED_REDSTONE, "Enriched Redstone");
        add(MekanismItems.ENRICHED_DIAMOND, "Enriched Diamond");
        add(MekanismItems.ENRICHED_OBSIDIAN, "Enriched Obsidian");
        add(MekanismItems.ENRICHED_GOLD, "Enriched Gold");
        add(MekanismItems.ENRICHED_TIN, "Enriched Tin");
        //Upgrades
        add(MekanismItems.SPEED_UPGRADE, "Speed Upgrade");
        add(MekanismItems.ENERGY_UPGRADE, "Energy Upgrade");
        add(MekanismItems.FILTER_UPGRADE, "Filter Upgrade");
        add(MekanismItems.MUFFLING_UPGRADE, "Muffling Upgrade");
        add(MekanismItems.GAS_UPGRADE, "Gas Upgrade");
        add(MekanismItems.ANCHOR_UPGRADE, "Anchor Upgrade");
        //Alloys
        add(MekanismItems.INFUSED_ALLOY, "Infused Alloy");
        add(MekanismItems.REINFORCED_ALLOY, "Reinforced Alloy");
        add(MekanismItems.ATOMIC_ALLOY, "Atomic Alloy");
        //Ingots
        add(MekanismItems.REFINED_OBSIDIAN_INGOT, "Refined Obsidian Ingot");
        add(MekanismItems.BRONZE_INGOT, "Bronze Ingot");
        add(MekanismItems.REFINED_GLOWSTONE_INGOT, "Refined Glowstone Ingot");
        add(MekanismItems.STEEL_INGOT, "Steel Ingot");
        //Nuggets
        add(MekanismItems.REFINED_OBSIDIAN_NUGGET, "Refined Obsidian Nugget");
        add(MekanismItems.BRONZE_NUGGET, "Bronze Nugget");
        add(MekanismItems.REFINED_GLOWSTONE_NUGGET, "Refined Glowstone Nugget");
        add(MekanismItems.STEEL_NUGGET, "Steel Nugget");
        //Dusts
        add(MekanismItems.BRONZE_DUST, "Bronze Dust");
        add(MekanismItems.LAPIS_LAZULI_DUST, "Lapis Lazuli Dust");
        add(MekanismItems.COAL_DUST, "Coal Dust");
        add(MekanismItems.CHARCOAL_DUST, "Charcoal Dust");
        add(MekanismItems.QUARTZ_DUST, "Quartz Dust");
        add(MekanismItems.EMERALD_DUST, "Emerald Dust");
        add(MekanismItems.DIAMOND_DUST, "Diamond Dust");
        add(MekanismItems.NETHERITE_DUST, "Netherite Dust");
        add(MekanismItems.STEEL_DUST, "Steel Dust");
        add(MekanismItems.SULFUR_DUST, "Sulfur Dust");
        add(MekanismItems.LITHIUM_DUST, "Lithium Dust");
        add(MekanismItems.REFINED_OBSIDIAN_DUST, "Refined Obsidian Dust");
        add(MekanismItems.OBSIDIAN_DUST, "Obsidian Dust");
        add(MekanismItems.FLUORITE_DUST, "Fluorite Dust");
        //Scrap
        add(MekanismItems.DIRTY_NETHERITE_SCRAP, "Dirty Netherite Scrap");
        //Tiered stuff
        addTiered(MekanismItems.BASIC_CONTROL_CIRCUIT, MekanismItems.ADVANCED_CONTROL_CIRCUIT, MekanismItems.ELITE_CONTROL_CIRCUIT, MekanismItems.ULTIMATE_CONTROL_CIRCUIT, "Control Circuit");
        addTiered(MekanismItems.BASIC_TIER_INSTALLER, MekanismItems.ADVANCED_TIER_INSTALLER, MekanismItems.ELITE_TIER_INSTALLER, MekanismItems.ULTIMATE_TIER_INSTALLER, "Tier Installer");

        for (Cell<ResourceType, PrimaryResource, ItemRegistryObject<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            String resourceName = formatAndCapitalize(item.getColumnKey().getName());
            switch (item.getRowKey()) {
                case SHARD:
                    add(item.getValue(), resourceName + " Shard");
                    break;
                case CRYSTAL:
                    add(item.getValue(), resourceName + " Crystal");
                    break;
                case DUST:
                    add(item.getValue(), resourceName + " Dust");
                    break;
                case DIRTY_DUST:
                    add(item.getValue(), "Dirty " + resourceName + " Dust");
                    break;
                case CLUMP:
                    add(item.getValue(), resourceName + " Clump");
                    break;
                case INGOT:
                    add(item.getValue(), resourceName + " Ingot");
                    break;
                case NUGGET:
                    add(item.getValue(), resourceName + " Nugget");
                    break;
                default:
                    break;
            }
        }
    }

    private static String formatAndCapitalize(String s) {
        boolean isFirst = true;
        StringBuilder ret = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '_') {
                isFirst = true;
                ret.append(' ');
            } else {
                ret.append(isFirst ? Character.toUpperCase(c) : c);
                isFirst = false;
            }
        }
        return ret.toString();
    }

    private void addBlocks() {
        add(MekanismBlocks.BOILER_CASING, "Boiler Casing");
        add(MekanismBlocks.BOILER_VALVE, "Boiler Valve");
        add(MekanismBlocks.CARDBOARD_BOX, "Cardboard Box");
        add(MekanismBlocks.CHARGEPAD, "Chargepad");
        add(MekanismBlocks.CHEMICAL_CRYSTALLIZER, "Chemical Crystallizer");
        add(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, "Chemical Dissolution Chamber");
        add(MekanismBlocks.CHEMICAL_INFUSER, "Chemical Infuser");
        add(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, "Chemical Injection Chamber");
        add(MekanismBlocks.CHEMICAL_OXIDIZER, "Chemical Oxidizer");
        add(MekanismBlocks.CHEMICAL_WASHER, "Chemical Washer");
        add(MekanismBlocks.COMBINER, "Combiner");
        add(MekanismBlocks.CRUSHER, "Crusher");
        add(MekanismBlocks.DIGITAL_MINER, "Digital Miner");
        add(MekanismBlocks.DYNAMIC_TANK, "Dynamic Tank");
        add(MekanismBlocks.DYNAMIC_VALVE, "Dynamic Valve");
        add(MekanismBlocks.ELECTRIC_PUMP, "Electric Pump");
        add(MekanismBlocks.ELECTROLYTIC_SEPARATOR, "Electrolytic Separator");
        add(MekanismBlocks.ENERGIZED_SMELTER, "Energized Smelter");
        add(MekanismBlocks.ENRICHMENT_CHAMBER, "Enrichment Chamber");
        add(MekanismBlocks.FLUIDIC_PLENISHER, "Fluidic Plenisher");
        add(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, "Formulaic Assemblicator");
        add(MekanismBlocks.FUELWOOD_HEATER, "Fuelwood Heater");
        add(MekanismBlocks.INDUCTION_CASING, "Induction Casing");
        add(MekanismBlocks.INDUCTION_PORT, "Induction Port");
        add(MekanismBlocks.LASER, "Laser");
        add(MekanismBlocks.LASER_AMPLIFIER, "Laser Amplifier");
        add(MekanismBlocks.LASER_TRACTOR_BEAM, "Laser Tractor Beam");
        add(MekanismBlocks.LOGISTICAL_SORTER, "Logistical Sorter");
        add(MekanismBlocks.METALLURGIC_INFUSER, "Metallurgic Infuser");
        add(MekanismBlocks.OREDICTIONIFICATOR, "Oredictionificator");
        add(MekanismBlocks.OSMIUM_COMPRESSOR, "Osmium Compressor");
        add(MekanismBlocks.PERSONAL_CHEST, "Personal Chest");
        add(MekanismBlocks.PRECISION_SAWMILL, "Precision Sawmill");
        add(MekanismBlocks.PRESSURE_DISPERSER, "Pressure Disperser");
        add(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, "Pressurized Reaction Chamber");
        add(MekanismBlocks.PURIFICATION_CHAMBER, "Purification Chamber");
        add(MekanismBlocks.QUANTUM_ENTANGLOPORTER, "Quantum Entangloporter");
        add(MekanismBlocks.RESISTIVE_HEATER, "Resistive Heater");
        add(MekanismBlocks.MODIFICATION_STATION, "Modification Station");
        add(MekanismBlocks.ISOTOPIC_CENTRIFUGE, "Isotopic Centrifuge");
        add(MekanismBlocks.NUTRITIONAL_LIQUIFIER, "Nutritional Liquifier");
        add(MekanismBlocks.ROTARY_CONDENSENTRATOR, "Rotary Condensentrator");
        add(MekanismBlocks.SALT_BLOCK, "Salt Block");
        add(MekanismBlocks.SECURITY_DESK, "Security Desk");
        add(MekanismBlocks.SEISMIC_VIBRATOR, "Seismic Vibrator");
        add(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, "Solar Neutron Activator");
        add(MekanismBlocks.STEEL_CASING, "Steel Casing");
        add(MekanismBlocks.STRUCTURAL_GLASS, "Structural Glass");
        add(MekanismBlocks.SUPERHEATING_ELEMENT, "Superheating Element");
        add(MekanismBlocks.TELEPORTER, "Teleporter");
        add(MekanismBlocks.TELEPORTER_FRAME, "Teleporter Frame");
        add(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, "Thermal Evaporation Block");
        add(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, "Thermal Evaporation Controller");
        add(MekanismBlocks.THERMAL_EVAPORATION_VALVE, "Thermal Evaporation Valve");
        add(MekanismBlocks.RADIOACTIVE_WASTE_BARREL, "Radioactive Waste Barrel");
        add(MekanismBlocks.INDUSTRIAL_ALARM, "Industrial Alarm");
        add(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, "Antiprotonic Nucleosynthesizer");
        add(MekanismBlocks.QIO_DRIVE_ARRAY, "QIO Drive Array");
        add(MekanismBlocks.QIO_DASHBOARD, "QIO Dashboard");
        add(MekanismBlocks.QIO_IMPORTER, "QIO Importer");
        add(MekanismBlocks.QIO_EXPORTER, "QIO Exporter");
        add(MekanismBlocks.QIO_REDSTONE_ADAPTER, "QIO Redstone Adapter");
        add(MekanismBlocks.SPS_CASING, "SPS Casing");
        add(MekanismBlocks.SPS_PORT, "SPS Port");
        add(MekanismBlocks.SUPERCHARGED_COIL, "Supercharged Coil");
        //Bounding block (I don't think these lang keys actually will ever be used, but set them just in case)
        add(MekanismBlocks.BOUNDING_BLOCK, "Bounding Block");
        add(MekanismBlocks.ADVANCED_BOUNDING_BLOCK, "Advanced Bounding Block");
        //Ores
        for (OreType ore : EnumUtils.ORE_TYPES) {
            add(MekanismBlocks.ORES.get(ore), formatAndCapitalize(ore.getResource().getRegistrySuffix()) + " Ore");
        }
        //Storage blocks
        add(MekanismBlocks.BRONZE_BLOCK, "Bronze Block");
        add(MekanismBlocks.REFINED_OBSIDIAN_BLOCK, "Refined Obsidian");
        add(MekanismBlocks.CHARCOAL_BLOCK, "Charcoal Block");
        add(MekanismBlocks.REFINED_GLOWSTONE_BLOCK, "Refined Glowstone");
        add(MekanismBlocks.STEEL_BLOCK, "Steel Block");
        //Dynamic storage blocks
        for (Map.Entry<PrimaryResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            add(entry.getValue(), formatAndCapitalize(entry.getKey().getName()) + " Block");
        }

        //Tiered things
        addTiered(MekanismBlocks.BASIC_INDUCTION_CELL, MekanismBlocks.ADVANCED_INDUCTION_CELL, MekanismBlocks.ELITE_INDUCTION_CELL, MekanismBlocks.ULTIMATE_INDUCTION_CELL, "Induction Cell");
        addTiered(MekanismBlocks.BASIC_INDUCTION_PROVIDER, MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, MekanismBlocks.ELITE_INDUCTION_PROVIDER, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER, "Induction Provider");
        addTiered(MekanismBlocks.BASIC_BIN, MekanismBlocks.ADVANCED_BIN, MekanismBlocks.ELITE_BIN, MekanismBlocks.ULTIMATE_BIN, MekanismBlocks.CREATIVE_BIN, "Bin");
        addTiered(MekanismBlocks.BASIC_ENERGY_CUBE, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismBlocks.ULTIMATE_ENERGY_CUBE, MekanismBlocks.CREATIVE_ENERGY_CUBE, "Energy Cube");
        addTiered(MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ULTIMATE_FLUID_TANK, MekanismBlocks.CREATIVE_FLUID_TANK, "Fluid Tank");
        addTiered(MekanismBlocks.BASIC_CHEMICAL_TANK, MekanismBlocks.ADVANCED_CHEMICAL_TANK, MekanismBlocks.ELITE_CHEMICAL_TANK, MekanismBlocks.ULTIMATE_CHEMICAL_TANK, MekanismBlocks.CREATIVE_CHEMICAL_TANK, "Chemical Tank");
        //Factories
        for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
            for (FactoryType type : EnumUtils.FACTORY_TYPES) {
                add(MekanismBlocks.getFactory(tier, type), tier.getBaseTier().getSimpleName() + " " + capitalize(type.getRegistryNameComponent()) + " Factory");
            }
        }
        //Transmitters
        add(MekanismBlocks.RESTRICTIVE_TRANSPORTER, "Restrictive Transporter");
        add(MekanismBlocks.DIVERSION_TRANSPORTER, "Diversion Transporter");
        addTiered(MekanismBlocks.BASIC_UNIVERSAL_CABLE, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE, "Universal Cable");
        addTiered(MekanismBlocks.BASIC_MECHANICAL_PIPE, MekanismBlocks.ADVANCED_MECHANICAL_PIPE, MekanismBlocks.ELITE_MECHANICAL_PIPE, MekanismBlocks.ULTIMATE_MECHANICAL_PIPE, "Mechanical Pipe");
        addTiered(MekanismBlocks.BASIC_PRESSURIZED_TUBE, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, MekanismBlocks.ELITE_PRESSURIZED_TUBE, MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE, "Pressurized Tube");
        addTiered(MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER, "Logistical Transporter");
        addTiered(MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR, "Thermodynamic Conductor");
    }

    private void addFluids() {
        addFluid(MekanismFluids.HYDROGEN, "Liquid Hydrogen");
        addFluid(MekanismFluids.OXYGEN, "Liquid Oxygen");
        addFluid(MekanismFluids.CHLORINE, "Liquid Chlorine");
        addFluid(MekanismFluids.SULFUR_DIOXIDE, "Liquid Sulfur Dioxide");
        addFluid(MekanismFluids.SULFUR_TRIOXIDE, "Liquid Sulfur Trioxide");
        addFluid(MekanismFluids.SULFURIC_ACID, "Sulfuric Acid");
        addFluid(MekanismFluids.HYDROGEN_CHLORIDE, "Liquid Hydrogen Chloride");
        addFluid(MekanismFluids.HYDROFLUORIC_ACID, "Liquid Hydrofluoric Acid");
        addFluid(MekanismFluids.ETHENE, "Liquid Ethylene");
        addFluid(MekanismFluids.SODIUM, "Liquid Sodium");
        addFluid(MekanismFluids.BRINE, "Brine");
        addFluid(MekanismFluids.LITHIUM, "Liquid Lithium");
        addFluid(MekanismFluids.STEAM, "Liquid Steam");
        addFluid(MekanismFluids.HEAVY_WATER, "Heavy Water");
    }

    private void addEntities() {
        add(MekanismEntityTypes.FLAME, "Flamethrower Flame");
        add(MekanismEntityTypes.ROBIT, "Robit");
    }

    private void addGases() {
        add(MekanismAPI.EMPTY_GAS, "Empty");
        add(MekanismGases.HYDROGEN, "Hydrogen");
        add(MekanismGases.OXYGEN, "Oxygen");
        add(MekanismGases.STEAM, "Steam");
        add(MekanismGases.WATER_VAPOR, "Water Vapor");
        add(MekanismGases.CHLORINE, "Chlorine");
        add(MekanismGases.SULFUR_DIOXIDE, "Sulfur Dioxide");
        add(MekanismGases.SULFUR_TRIOXIDE, "Sulfur Trioxide");
        add(MekanismGases.SULFURIC_ACID, "Sulfuric Acid");
        add(MekanismGases.HYDROGEN_CHLORIDE, "Hydrogen Chloride");
        add(MekanismGases.HYDROFLUORIC_ACID, "Hydrofluoric Acid");
        add(MekanismGases.URANIUM_OXIDE, "Uranium Oxide");
        add(MekanismGases.URANIUM_HEXAFLUORIDE, "Uranium Hexafluoride");
        add(MekanismGases.ETHENE, "Ethylene");
        add(MekanismGases.SODIUM, "Sodium");
        add(MekanismGases.SUPERHEATED_SODIUM, "Superheated Sodium");
        add(MekanismGases.BRINE, "Gaseous Brine");
        add(MekanismGases.LITHIUM, "Lithium");
        add(MekanismGases.LIQUID_OSMIUM, "Liquid Osmium");
        add(MekanismGases.FISSILE_FUEL, "Fissile Fuel");
        add(MekanismGases.NUCLEAR_WASTE, "Nuclear Waste");
        add(MekanismGases.SPENT_NUCLEAR_WASTE, "Spent Nuclear Waste");
        add(MekanismGases.ANTIMATTER, "Antimatter");
        add(MekanismGases.PLUTONIUM, "Plutonium");
        add(MekanismGases.POLONIUM, "Polonium");
        add(MekanismGases.NUTRITIONAL_PASTE, "Nutritional Paste");
    }

    private void addInfusionTypes() {
        add(MekanismAPI.EMPTY_INFUSE_TYPE, "Empty");
        add(MekanismInfuseTypes.CARBON, "Carbon");
        add(MekanismInfuseTypes.REDSTONE, "Redstone");
        add(MekanismInfuseTypes.DIAMOND, "Diamond");
        add(MekanismInfuseTypes.REFINED_OBSIDIAN, "Refined Obsidian");
        add(MekanismInfuseTypes.GOLD, "Gold");
        add(MekanismInfuseTypes.TIN, "Tin");
        add(MekanismInfuseTypes.FUNGI, "Fungi");
        add(MekanismInfuseTypes.BIO, "Biomass");
    }

    private void addPigments() {
        add(MekanismAPI.EMPTY_PIGMENT, "Empty");
    }

    private void addSlurries() {
        add(MekanismAPI.EMPTY_SLURRY, "Empty");
        for (Map.Entry<PrimaryResource, SlurryRegistryObject<Slurry, Slurry>> entry : MekanismSlurries.PROCESSED_RESOURCES.entrySet()) {
            addSlurry(entry.getValue(), formatAndCapitalize(entry.getKey().getName()));
        }
    }

    private void addSlurry(SlurryRegistryObject<Slurry, Slurry> slurryRO, String name) {
        add(slurryRO.getDirtySlurry(), "Dirty " + name + " Slurry");
        add(slurryRO.getCleanSlurry(), "Clean " + name + " Slurry");
    }

    private void addDamageSources() {
        add(MekanismDamageSource.LASER, "%s was incinerated.");
        add(MekanismDamageSource.RADIATION, "%s was killed by radiation poisoning.");
    }

    private void addSubtitles() {
        //Tiles
        add(MekanismSounds.CHARGEPAD, "Chargepad hums");
        add(MekanismSounds.CHEMICAL_CRYSTALLIZER, "Crystallizer hums");
        add(MekanismSounds.CHEMICAL_DISSOLUTION_CHAMBER, "Dissolution Chamber hums");
        add(MekanismSounds.CHEMICAL_INFUSER, "Chemical Infuser hums");
        add(MekanismSounds.CHEMICAL_INJECTION_CHAMBER, "Injection Chamber processes");
        add(MekanismSounds.CHEMICAL_OXIDIZER, "Oxidizer hums");
        add(MekanismSounds.CHEMICAL_WASHER, "Washer hums");
        add(MekanismSounds.COMBINER, "Combiner hums");
        add(MekanismSounds.OSMIUM_COMPRESSOR, "Compressor hums");
        add(MekanismSounds.CRUSHER, "Crusher clangs");
        add(MekanismSounds.ELECTROLYTIC_SEPARATOR, "Separator separates");
        add(MekanismSounds.ENRICHMENT_CHAMBER, "Enricher hums");
        add(MekanismSounds.LASER, "Laser hums");
        add(MekanismSounds.LOGISTICAL_SORTER, "Sorter clicks");
        add(MekanismSounds.METALLURGIC_INFUSER, "Metallurgic infuser hums");
        add(MekanismSounds.PRECISION_SAWMILL, "Sawmill cuts");
        add(MekanismSounds.PRESSURIZED_REACTION_CHAMBER, "Reaction chamber hums");
        add(MekanismSounds.PURIFICATION_CHAMBER, "Purifier hums");
        add(MekanismSounds.RESISTIVE_HEATER, "Heater hums");
        add(MekanismSounds.ROTARY_CONDENSENTRATOR, "Condensentrator rotates");
        add(MekanismSounds.ENERGIZED_SMELTER, "Smelter whines");
        add(MekanismSounds.ISOTOPIC_CENTRIFUGE, "Centrifuge spins");
        add(MekanismSounds.NUTRITIONAL_LIQUIFIER, "Nutrients liquified");
        add(MekanismSounds.INDUSTRIAL_ALARM, "Alarm sounds");
        add(MekanismSounds.ANTIPROTONIC_NUCLEOSYNTHESIZER, "Nucleosynthesizer hums");
        add(MekanismSounds.SPS, "SPS hums");
        //Gear
        add(MekanismSounds.FLAMETHROWER_IDLE, "Flamethrower hisses");
        add(MekanismSounds.FLAMETHROWER_ACTIVE, "Flamethrower burns");
        add(MekanismSounds.SCUBA_MASK, "Air flows");
        add(MekanismSounds.JETPACK, "Jetpack burns");
        add(MekanismSounds.HYDRAULIC, "Hydraulic shifts");
        add(MekanismSounds.GRAVITATIONAL_MODULATION_UNIT, "Gravity modulates");
        //Geiger
        add(MekanismSounds.GEIGER_SLOW, "Geiger counter clicks slowly");
        add(MekanismSounds.GEIGER_MEDIUM, "Geiger Counter clicks");
        add(MekanismSounds.GEIGER_ELEVATED, "Elevated Geiger Counter clicks");
        add(MekanismSounds.GEIGER_FAST, "Constant Geiger Counter clicks");
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
        add(MekanismLang.TRANSMISSION_TYPE_ENERGY, "Energy");
        add(MekanismLang.TRANSMISSION_TYPE_FLUID, "Fluids");
        add(MekanismLang.TRANSMISSION_TYPE_GAS, "Gases");
        add(MekanismLang.TRANSMISSION_TYPE_INFUSION, "Infuse Types");
        add(MekanismLang.TRANSMISSION_TYPE_PIGMENT, "Pigments");
        add(MekanismLang.TRANSMISSION_TYPE_SLURRY, "Slurries");
        add(MekanismLang.TRANSMISSION_TYPE_ITEM, "Items");
        add(MekanismLang.TRANSMISSION_TYPE_HEAT, "Heat");
        //Chemical Attributes
        add(APILang.CHEMICAL_ATTRIBUTE_RADIATION, " - Radioactivity: %s sV/h");
        add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_EFFICIENCY, " - Coolant Efficiency: %s");
        add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_ENTHALPY, " - Thermal Enthalpy: %s J/mB");
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
        //Equipment
        add(MekanismLang.HEAD, "Head");
        add(MekanismLang.BODY, "Body");
        add(MekanismLang.LEGS, "Legs");
        add(MekanismLang.FEET, "Feet");
        add(MekanismLang.MAINHAND, "Hand 1");
        add(MekanismLang.OFFHAND, "Hand 2");
        //Multiblock
        add(MekanismLang.MULTIBLOCK_INVALID_FRAME, "Couldn't create frame, invalid block at %s.");
        add(MekanismLang.MULTIBLOCK_INVALID_INNER, "Couldn't validate center, found invalid block at %s.");
        add(MekanismLang.MULTIBLOCK_INVALID_CONTROLLER_CONFLICT, "Controller conflict: found extra controller at %s.");
        add(MekanismLang.MULTIBLOCK_INVALID_NO_CONTROLLER, "Couldn't form, no controller found.");
        //SPS
        add(MekanismLang.SPS, "Supercritical Phase Shifter");
        add(MekanismLang.SPS_INVALID_DISCONNECTED_COIL, "Couldn't form, found a coil without a connection to an SPS Port.");
        add(MekanismLang.SPS_PORT_MODE, "Toggled SPS Port mode to: %s.");
        add(MekanismLang.SPS_ENERGY_INPUT, "Energy Input: %s/t");
        //Boiler
        add(MekanismLang.BOILER_INVALID_AIR_POCKETS, "Couldn't form, found disconnected interior air pockets.");
        add(MekanismLang.BOILER_INVALID_EXTRA_DISPERSER, "Couldn't form, found invalid Pressure Dispersers.");
        add(MekanismLang.BOILER_INVALID_MISSING_DISPERSER, "Couldn't form, expected but didn't find Pressure Disperser at %s.");
        add(MekanismLang.BOILER_INVALID_NO_DISPERSER, "Couldn't form, no Pressure Disperser layer found.");
        add(MekanismLang.BOILER_INVALID_SUPERHEATING, "Couldn't form, invalid Superheating Element arrangement.");
        //Conversion
        add(MekanismLang.CONVERSION_ENERGY, "Item to Energy");
        add(MekanismLang.CONVERSION_GAS, "Item to Gas");
        add(MekanismLang.CONVERSION_INFUSION, "Item to Infuse Type");
        //QIO stuff
        add(MekanismLang.SET_FREQUENCY, "Set Frequency");
        add(MekanismLang.QIO_FREQUENCY_SELECT, "QIO Frequency Select");
        add(MekanismLang.QIO_ITEMS_DETAIL, "Items: %s / %s");
        add(MekanismLang.QIO_TYPES_DETAIL, "Types: %s / %s");
        add(MekanismLang.QIO_ITEMS, "Items");
        add(MekanismLang.QIO_TYPES, "Types");
        add(MekanismLang.QIO_TRIGGER_COUNT, "Trigger count: %s");
        add(MekanismLang.QIO_STORED_COUNT, "Stored count: %s");
        add(MekanismLang.QIO_ITEM_TYPE_UNDEFINED, "Item type undefined");
        add(MekanismLang.QIO_IMPORT_WITHOUT_FILTER, "Import Without Filter:");
        add(MekanismLang.QIO_EXPORT_WITHOUT_FILTER, "Export Without Filter:");
        add(MekanismLang.QIO_COMPENSATE_TOOLTIP, "What are you trying to compensate for?");
        add(MekanismLang.LIST_SORT_COUNT, "Count");
        add(MekanismLang.LIST_SORT_NAME, "Name");
        add(MekanismLang.LIST_SORT_MOD, "Mod");
        add(MekanismLang.LIST_SORT_NAME_DESC, "Sort items by name.");
        add(MekanismLang.LIST_SORT_COUNT_DESC, "Sort items by count.");
        add(MekanismLang.LIST_SORT_MOD_DESC, "Sort items by mod.");
        add(MekanismLang.LIST_SORT_ASCENDING_DESC, "Sort items in ascending order.");
        add(MekanismLang.LIST_SORT_DESCENDING_DESC, "Sort items in descending order.");
        add(MekanismLang.LIST_SEARCH, "Search:");
        add(MekanismLang.LIST_SORT, "Sort:");
        //JEI
        add(MekanismLang.JEI_AMOUNT_WITH_CAPACITY, "%s / %s mB");
        //Key
        add(MekanismLang.KEY_HAND_MODE, "Item Mode Switch");
        add(MekanismLang.KEY_HEAD_MODE, "Head Mode Switch");
        add(MekanismLang.KEY_CHEST_MODE, "Chest Mode Switch");
        add(MekanismLang.KEY_LEGS_MODE, "Legs Mode Switch");
        add(MekanismLang.KEY_FEET_MODE, "Feet Mode Switch");
        add(MekanismLang.KEY_DETAILS_MODE, "Show Details");
        add(MekanismLang.KEY_DESCRIPTION_MODE, "Show Description");
        add(MekanismLang.KEY_BOOST, "Boost");
        add(MekanismLang.KEY_MODULE_TWEAKER, "Module Tweaker");
        add(MekanismLang.KEY_HUD, "Show HUD");
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
        add(MekanismLang.MAY_4_LINE_ONE, "May the 4th be with you, %s!");
        //Generic
        add(APILang.GENERIC, "%s");
        add(MekanismLang.GENERIC_WITH_COMMA, "%s, %s");
        add(MekanismLang.GENERIC_STORED, "%s: %s");
        add(MekanismLang.GENERIC_STORED_MB, "%s: %s mB");
        add(MekanismLang.GENERIC_MB, "%s mB");
        add(MekanismLang.GENERIC_PRE_COLON, "%s:");
        add(MekanismLang.GENERIC_SQUARE_BRACKET, "[%s]");
        add(MekanismLang.GENERIC_PARENTHESIS, "(%s)");
        add(MekanismLang.GENERIC_WITH_PARENTHESIS, "%s (%s)");
        add(MekanismLang.GENERIC_WITH_TWO_PARENTHESIS, "%s (%s) (%s)");
        add(MekanismLang.GENERIC_FRACTION, "%s/%s");
        add(MekanismLang.GENERIC_TRANSFER, "- %s (%s)");
        add(MekanismLang.GENERIC_PER_TICK, "%s/t");
        add(MekanismLang.GENERIC_PRE_STORED, "%s %s: %s");
        add(MekanismLang.GENERIC_BLOCK_POS, "%s, %s, %s");
        add(MekanismLang.GENERIC_HEX, "#%s");
        //Directions
        add(APILang.DOWN, "Down");
        add(APILang.UP, "Up");
        add(APILang.NORTH, "North");
        add(APILang.SOUTH, "South");
        add(APILang.WEST, "West");
        add(APILang.EAST, "East");
        add(MekanismLang.NORTH_SHORT, "N");
        add(MekanismLang.SOUTH_SHORT, "S");
        add(MekanismLang.WEST_SHORT, "W");
        add(MekanismLang.EAST_SHORT, "E");
        //Relative sides
        add(APILang.FRONT, "Front");
        add(APILang.LEFT, "Left");
        add(APILang.RIGHT, "Right");
        add(APILang.BACK, "Back");
        add(APILang.TOP, "Top");
        add(APILang.BOTTOM, "Bottom");
        //Hold for
        add(MekanismLang.HOLD_FOR_DETAILS, "Hold %s for details.");
        add(MekanismLang.HOLD_FOR_DESCRIPTION, "Hold %s for a description.");
        add(MekanismLang.HOLD_FOR_MODULES, "Hold %s for installed modules.");
        add(MekanismLang.HOLD_FOR_SUPPORTED_ITEMS, "Hold %s for supporting items.");
        //Commands
        add(MekanismLang.COMMAND_CHUNK_WATCH, "Chunk (%s) added to watch list.");
        add(MekanismLang.COMMAND_CHUNK_UNWATCH, "Chunk (%s) removed from watch list.");
        add(MekanismLang.COMMAND_CHUNK_CLEAR, "%s chunks removed from watch list.");
        add(MekanismLang.COMMAND_CHUNK_FLUSH, "%s chunks unloaded.");
        add(MekanismLang.COMMAND_CHUNK_LOADED, "Loaded chunk (%s).");
        add(MekanismLang.COMMAND_CHUNK_UNLOADED, "Unloaded chunk (%s).");
        add(MekanismLang.COMMAND_DEBUG, "Toggled debug mode: %s.");
        add(MekanismLang.COMMAND_TEST_RULES, "Enabled keepInventory, and disabled doMobSpawning, doDaylightCycle, doWeatherCycle and mobGriefing!");
        add(MekanismLang.COMMAND_TP, "Teleported to (%s) - saved last position on stack.");
        add(MekanismLang.COMMAND_TPOP, "Returned to (%s); %s positions on stack.");
        add(MekanismLang.COMMAND_ERROR_TPOP_EMPTY, "No positions on stack.");
        add(MekanismLang.COMMAND_BUILD_REMOVED, "Build successfully removed.");
        add(MekanismLang.COMMAND_BUILD_BUILT, "Finished building: %s.");
        add(MekanismLang.COMMAND_ERROR_BUILD_MISS, "No valid target found.");
        add(MekanismLang.COMMAND_RADIATION_ADD, "Added %s radiation at (%s) in %s.");
        add(MekanismLang.COMMAND_RADIATION_GET, "Current radiation at (%s) in %s: %s");
        add(MekanismLang.COMMAND_RADIATION_CLEAR, "Cleared player radiation.");
        add(MekanismLang.COMMAND_RADIATION_CLEAR_PLAYER, "Cleared player radiation for: %s.");
        add(MekanismLang.COMMAND_RADIATION_REMOVE_ALL, "Removed all radiation sources.");
        add(MekanismLang.COMMAND_RETROGEN_CHUNK_QUEUED, "Queued chunk (%s) in %s for retrogen.");
        add(MekanismLang.COMMAND_ERROR_RETROGEN_DISABLED, "Retrogen is disabled, please enable it in the config.");
        add(MekanismLang.COMMAND_ERROR_RETROGEN_FAILURE, "Failed to queue any chunks for retrogen.");
        //Tooltip stuff
        add(MekanismLang.MODE, "Mode: %s");
        add(MekanismLang.FIRE_MODE, "Fire Mode: %s");
        add(MekanismLang.BUCKET_MODE, "Bucket Mode: %s");
        add(MekanismLang.STORED_ENERGY, "Stored energy: %s");
        add(MekanismLang.STORED, "Stored %s: %s");
        add(MekanismLang.STORED_MB_PERCENTAGE, "Stored %s: %s mB (%s)");
        add(MekanismLang.ITEM_AMOUNT, "Item amount: %s");
        add(MekanismLang.FLOWING, "Flowing: %s");
        add(MekanismLang.INVALID, "(Invalid)");
        add(MekanismLang.HAS_INVENTORY, "Inventory: %s");
        add(MekanismLang.NO_GAS, "No gas stored.");
        add(MekanismLang.FREE_RUNNERS_MODE, "Runners Mode: %s");
        add(MekanismLang.JETPACK_MODE, "Jetpack Mode: %s");
        add(MekanismLang.SCUBA_TANK_MODE, "Scuba Tank: %s");
        add(MekanismLang.FREE_RUNNERS_STORED, "Runners Energy: %s");
        add(MekanismLang.FLAMETHROWER_STORED, "Flamethrower: %s");
        add(MekanismLang.JETPACK_STORED, "Jetpack Fuel: %s");
        add(MekanismLang.PROGRESS, "Progress: %s");
        add(MekanismLang.PROCESS_RATE, "Process Rate: %s");
        add(MekanismLang.PROCESS_RATE_MB, "Process Rate: %s mB/t");
        add(MekanismLang.TICKS_REQUIRED, "Ticks Required: %s");
        //Gui stuff
        add(MekanismLang.WIDTH, "Width");
        add(MekanismLang.HEIGHT, "Height");
        add(MekanismLang.MIN, "Min: %s");
        add(MekanismLang.MAX, "Max: %s");
        add(MekanismLang.INFINITE, "Infinite");
        add(MekanismLang.NONE, "None");
        add(MekanismLang.EMPTY, "Empty");
        add(MekanismLang.MAX_OUTPUT, "Max Output: %s/t");
        add(MekanismLang.STORING, "Storing: %s");
        add(MekanismLang.DISSIPATED_RATE, "Dissipated: %s/t");
        add(MekanismLang.TRANSFERRED_RATE, "Transferred: %s/t");
        add(MekanismLang.FUEL, "Fuel: %s");
        add(MekanismLang.VOLUME, "Volume: %s");
        add(MekanismLang.NO_FLUID, "No fluid");
        add(MekanismLang.CHEMICAL, "Chemical: %s");
        add(MekanismLang.GAS, "Gas: %s");
        add(MekanismLang.INFUSE_TYPE, "Infuse Type: %s");
        add(MekanismLang.PIGMENT, "Pigment: %s");
        add(MekanismLang.SLURRY, "Slurry: %s");
        add(MekanismLang.LIQUID, "Liquid: %s");
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
        add(MekanismLang.TRUE, "True");
        add(MekanismLang.FALSE, "False");
        add(MekanismLang.CLOSE, "Close");
        add(MekanismLang.RADIATION_DOSE, "Radiation Dose: %s");
        add(MekanismLang.RADIATION_EXPOSURE, "Radiation Exposure: %s");
        add(MekanismLang.RGB, "RGB:");
        add(MekanismLang.COLOR_PICKER, "Color Picker");
        add(MekanismLang.HELMET_OPTIONS, "Helmet Options");
        add(MekanismLang.HUD_OVERLAY, "HUD Overlay:");
        add(MekanismLang.OPACITY, "Opacity");
        add(MekanismLang.DEFAULT, "Default");
        add(MekanismLang.WARNING, "Warning");
        add(MekanismLang.DANGER, "Danger");
        add(MekanismLang.COMPASS, "Compass");
        //Laser Amplifier
        add(MekanismLang.ENTITY_DETECTION, "Entity Detection");
        add(MekanismLang.ENERGY_CONTENTS, "Energy Contents");
        add(MekanismLang.REDSTONE_OUTPUT, "Redstone Output: %s");
        //Frequency
        add(MekanismLang.FREQUENCY, "Frequency: %s");
        add(MekanismLang.NO_FREQUENCY, "No frequency");
        add(MekanismLang.FREQUENCY_DELETE_CONFIRM, "Are you sure you want to delete this frequency? This can't be undone.");
        //Owner
        add(MekanismLang.NOW_OWN, "You now own this item.");
        add(MekanismLang.OWNER, "Owner: %s");
        add(MekanismLang.NO_OWNER, "No Owner");
        //Tab
        add(MekanismLang.MAIN_TAB, "Main");
        //Evaporation
        add(MekanismLang.EVAPORATION_HEIGHT, "Height: %s");
        add(MekanismLang.FLUID_PRODUCTION, "Production: %s mB/t");
        add(MekanismLang.EVAPORATION_PLANT, "Thermal Evaporation Plant");
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
        add(MekanismLang.DICTIONARY_BLOCK_TAGS_FOUND, "Block Tag(s) found:");
        add(MekanismLang.DICTIONARY_FLUID_TAGS_FOUND, "Fluid Tag(s) found:");
        add(MekanismLang.DICTIONARY_ENTITY_TYPE_TAGS_FOUND, "Entity Type Tag(s) found:");
        add(MekanismLang.DICTIONARY_TILE_ENTITY_TYPE_TAGS_FOUND, "Tile Entity Type Tag(s) found:");
        add(MekanismLang.DICTIONARY_TAG_TYPE, "Tag Type:");
        add(MekanismLang.DICTIONARY_ITEM, "Item");
        add(MekanismLang.DICTIONARY_ITEM_DESC, "Display Item Tags");
        add(MekanismLang.DICTIONARY_BLOCK, "Block");
        add(MekanismLang.DICTIONARY_BLOCK_DESC, "Display Block Tags");
        add(MekanismLang.DICTIONARY_FLUID, "Fluid");
        add(MekanismLang.DICTIONARY_FLUID_DESC, "Display Fluid Tags");
        add(MekanismLang.DICTIONARY_ENTITY_TYPE, "Entity Type");
        add(MekanismLang.DICTIONARY_ENTITY_TYPE_DESC, "Display Entity Type Tags");
        add(MekanismLang.DICTIONARY_POTION, "Potion");
        add(MekanismLang.DICTIONARY_POTION_DESC, "Display Potion Tags");
        add(MekanismLang.DICTIONARY_ENCHANTMENT, "Enchantment");
        add(MekanismLang.DICTIONARY_ENCHANTMENT_DESC, "Display Enchantment Tags");
        add(MekanismLang.DICTIONARY_TILE_ENTITY_TYPE, "Tile Entity Type");
        add(MekanismLang.DICTIONARY_TILE_ENTITY_TYPE_DESC, "Display Tile Entity Type Tags");
        add(MekanismLang.DICTIONARY_GAS, "Gas");
        add(MekanismLang.DICTIONARY_GAS_DESC, "Display Gas Tags");
        add(MekanismLang.DICTIONARY_INFUSE_TYPE, "Infuse Type");
        add(MekanismLang.DICTIONARY_INFUSE_TYPE_DESC, "Display Infuse Type Tags");
        add(MekanismLang.DICTIONARY_PIGMENT, "Pigment");
        add(MekanismLang.DICTIONARY_PIGMENT_DESC, "Display Pigment Tags");
        add(MekanismLang.DICTIONARY_SLURRY, "Slurry");
        add(MekanismLang.DICTIONARY_SLURRY_DESC, "Display Slurry Tags");
        //Oredictionificator
        add(MekanismLang.LAST_ITEM, "Last Item");
        add(MekanismLang.NEXT_ITEM, "Next Item");
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
        add(MekanismLang.ACTIVE, "Active");
        add(MekanismLang.DISABLED, "Disabled");
        add(MekanismLang.ON_CAPS, "ON");
        add(MekanismLang.OFF_CAPS, "OFF");
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
        add(MekanismLang.PUMP_RATE_MB, "Pump Rate: %s mB/t");
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
        add(MekanismLang.SIZE_MODE, "Size Mode");
        add(MekanismLang.SIZE_MODE_CONFLICT, "Size Mode - has no effect currently, because single item mode is turned on.");
        add(MekanismLang.FUZZY_MODE, "Fuzzy Mode");
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
        add(MekanismLang.ITEM_FILTER_MAX_LESS_THAN_MIN, "Max < min");
        add(MekanismLang.ITEM_FILTER_OVER_SIZED, "Max > 64");
        add(MekanismLang.ITEM_FILTER_SIZE_MISSING, "Max/min");
        add(MekanismLang.OREDICTIONIFICATOR_FILTER, "Oredictionificator Filter");
        add(MekanismLang.OREDICTIONIFICATOR_FILTER_INCOMPATIBLE_TAG, "Incompatible tag");
        //Seismic Vibrator
        add(MekanismLang.CHUNK, "Chunk: %s, %s");
        add(MekanismLang.VIBRATING, "Vibrating");
        //Seismic Reader
        add(MekanismLang.NEEDS_ENERGY, "Not enough energy to interpret vibration");
        add(MekanismLang.NO_VIBRATIONS, "Unable to discover any vibrations");
        add(MekanismLang.ABUNDANCY, "Abundancy: %s");
        //Redstone Control
        add(MekanismLang.REDSTONE_CONTROL_DISABLED, "Redstone Detection: IGNORED");
        add(MekanismLang.REDSTONE_CONTROL_HIGH, "Redstone Detection: NORMAL");
        add(MekanismLang.REDSTONE_CONTROL_LOW, "Redstone Detection: INVERTED");
        add(MekanismLang.REDSTONE_CONTROL_PULSE, "Redstone Detection: PULSE");
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
        add(MekanismLang.CHEMICAL_NETWORK, "ChemicalNetwork");
        add(MekanismLang.HEAT_NETWORK, "HeatNetwork");
        add(MekanismLang.ENERGY_NETWORK, "EnergyNetwork");
        add(MekanismLang.NO_NETWORK, "No Network");
        add(MekanismLang.HEAT_NETWORK_STORED, "%s above ambient");
        add(MekanismLang.HEAT_NETWORK_FLOW, "%s transferred to acceptors, %s lost to environment.");
        add(MekanismLang.HEAT_NETWORK_FLOW_EFFICIENCY, "%s transferred to acceptors, %s lost to environment, %s efficiency.");
        add(MekanismLang.FLUID_NETWORK_NEEDED, "%s buckets");
        add(MekanismLang.NETWORK_MB_PER_TICK, "%s mB/t");
        add(MekanismLang.NETWORK_MB_STORED, "%s (%s mB)");
        //Button
        add(MekanismLang.BUTTON_CONFIRM, "Confirm");
        add(MekanismLang.BUTTON_START, "Start");
        add(MekanismLang.BUTTON_STOP, "Stop");
        add(MekanismLang.BUTTON_CONFIG, "Config");
        add(MekanismLang.BUTTON_REMOVE, "Remove");
        add(MekanismLang.BUTTON_CANCEL, "Cancel");
        add(MekanismLang.BUTTON_SAVE, "Save");
        add(MekanismLang.BUTTON_SET, "Set");
        add(MekanismLang.BUTTON_DELETE, "Delete");
        add(MekanismLang.BUTTON_OPTIONS, "Options");
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
        add(MekanismLang.MATRIX_OUTPUT_AMOUNT, "Output: %s");
        add(MekanismLang.MATRIX_OUTPUT_RATE, "Output: %s/t");
        add(MekanismLang.MATRIX_OUTPUTTING_RATE, "Outputting: %s/t");
        add(MekanismLang.MATRIX_INPUT_AMOUNT, "Input: %s");
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
        add(MekanismLang.BOILER_VALVE_MODE_CHANGE, "Valve mode changed to: %s");
        add(MekanismLang.BOILER_VALVE_MODE_INPUT, "input only");
        add(MekanismLang.BOILER_VALVE_MODE_OUTPUT_COOLANT, "output coolant");
        add(MekanismLang.BOILER_VALVE_MODE_OUTPUT_STEAM, "output steam");
        add(MekanismLang.BOILER_WATER_TANK, "Water Tank");
        add(MekanismLang.BOILER_STEAM_TANK, "Steam Tank");
        add(MekanismLang.BOILER_HEATED_COOLANT_TANK, "Heated Coolant Tank");
        add(MekanismLang.BOILER_COOLANT_TANK, "Coolant Tank");
        //Temperature
        add(MekanismLang.TEMPERATURE, "Temp: %s");
        add(MekanismLang.TEMPERATURE_LONG, "Temperature: %s");
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
        add(MekanismLang.NETWORK_READER_TEMPERATURE, " *Temperature: %s");
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
        add(MekanismLang.SORTER_AUTO_EJECT_DESCRIPTION, "Ejects unfiltered items automatically to connected inventories, using the default configuration.");
        //Side data/config
        add(MekanismLang.SIDE_DATA_NONE, "None");
        add(MekanismLang.SIDE_DATA_INPUT, "Input");
        add(MekanismLang.SIDE_DATA_INPUT_1, "Input (1)");
        add(MekanismLang.SIDE_DATA_INPUT_2, "Input (2)");
        add(MekanismLang.SIDE_DATA_OUTPUT, "Output");
        add(MekanismLang.SIDE_DATA_OUTPUT_1, "Output (1)");
        add(MekanismLang.SIDE_DATA_OUTPUT_2, "Output (2)");
        add(MekanismLang.SIDE_DATA_INPUT_OUTPUT, "Input/Output");
        add(MekanismLang.SIDE_DATA_ENERGY, "Energy");
        add(MekanismLang.SIDE_DATA_EXTRA, "Extra");
        //Free runner modes
        add(MekanismLang.FREE_RUNNER_MODE_CHANGE, "Free runner mode changed to: %s");
        add(MekanismLang.FREE_RUNNER_NORMAL, "Regular");
        add(MekanismLang.FREE_RUNNER_DISABLED, "Disabled");
        //Jetpack Modes
        add(MekanismLang.JETPACK_MODE_CHANGE, "Jetpack mode changed to: %s");
        add(MekanismLang.JETPACK_NORMAL, "Regular");
        add(MekanismLang.JETPACK_HOVER, "Hover");
        add(MekanismLang.JETPACK_DISABLED, "Disabled");
        //Disassembler Mode
        add(MekanismLang.DISASSEMBLER_MODE_CHANGE, "Mode toggled to: %s (%s)");
        add(MekanismLang.DISASSEMBLER_EFFICIENCY, "Efficiency: %s");
        add(MekanismLang.DISASSEMBLER_NORMAL, "normal");
        add(MekanismLang.DISASSEMBLER_SLOW, "slow");
        add(MekanismLang.DISASSEMBLER_FAST, "fast");
        add(MekanismLang.DISASSEMBLER_VEIN, "vein");
        add(MekanismLang.DISASSEMBLER_EXTENDED_VEIN, "extended vein mining");
        add(MekanismLang.DISASSEMBLER_OFF, "off");
        //Flamethrower Modes
        add(MekanismLang.FLAMETHROWER_MODE_CHANGE, "Flamethrower mode changed to: %s");
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
        add(MekanismLang.DESCRIPTION_QIO_DRIVE_ARRAY, "The foundation of any Quantum Item Orchestration system. QIO Drives are stored here.");
        add(MekanismLang.DESCRIPTION_QIO_DASHBOARD, "A placeable monitor used to access an Quantum Item Orchestration system's contents.");
        add(MekanismLang.DESCRIPTION_QIO_IMPORTER, "A QIO-linked item import unit. Place on a block and import its contents to your QIO system.");
        add(MekanismLang.DESCRIPTION_QIO_EXPORTER, "A QIO-linked item export unit. Place on a block and export contents from your QIO system to the block.");
        add(MekanismLang.DESCRIPTION_QIO_REDSTONE_ADAPTER, "A QIO-linked redstone adapter. Use to monitor your QIO system's contents.");
        add(MekanismLang.DESCRIPTION_DICTIONARY, "A tool used for viewing the tags of various components such as: items, blocks, and fluids.");
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
        add(MekanismLang.DESCRIPTION_PRESSURE_DISPERSER, "A block used to disperse steam throughout a multiblock structure. These should form a gapless, horizontal plane in order to properly control steam flow.");
        add(MekanismLang.DESCRIPTION_BOILER_CASING, "A pressure-resistant, dense casing used in the creation of a Thermoelectric Boiler multiblock.");
        add(MekanismLang.DESCRIPTION_BOILER_VALVE, "A valve that can be placed on a Thermoelectric Boiler multiblock, allowing for the insertion of energy and water along with the extraction of produced steam.");
        add(MekanismLang.DESCRIPTION_SECURITY_DESK, "A central control hub for managing the security of all your owned machinery.");
        add(MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, "A simple machine used to enrich ores into two of their dust counterparts, as well as perform many other operations.");
        add(MekanismLang.DESCRIPTION_OSMIUM_COMPRESSOR, "A fairly advanced machine used to compress osmium into various dusts in order to create their ingot counterparts.");
        add(MekanismLang.DESCRIPTION_COMBINER, "A machine used to combine items together. For example, dusts and cobblestone to form their ore counterparts.");
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
        add(MekanismLang.DESCRIPTION_MODIFICATION_STATION, "An advanced workbench capable of installing and removing modules from modular equipment (i.e. MekaSuit!)");
        add(MekanismLang.DESCRIPTION_ISOTOPIC_CENTRIFUGE, "A machine with one single purpose: to spin its contents really, REALLY fast.");
        add(MekanismLang.DESCRIPTION_QUANTUM_ENTANGLOPORTER, "A highly-advanced block capable of transmitting any practical resource across long distances and dimensions.");
        add(MekanismLang.DESCRIPTION_NUTRITIONAL_LIQUIFIER, "A machine that is capable of processing any foods into non-dangerous, easily-digestible Nutritional Paste.");
        add(MekanismLang.DESCRIPTION_ANTIPROTONIC_NUCLEOSYNTHESIZER, "A machine which uses bits of antimatter and mass amounts of energy to atomically transmute various resources.");
        add(MekanismLang.DESCRIPTION_RADIOACTIVE_WASTE_BARREL, "A barrel that can be used to 'safety' store radioactive waste. WARNING: breaking this barrel will release its contents into the atmosphere.");
        add(MekanismLang.DESCRIPTION_INDUSTRIAL_ALARM, "Not just your everyday alarm... this is an 'industrial' alarm!");
        add(MekanismLang.DESCRIPTION_ENERGY_CUBE, "An advanced device for storing and distributing energy.");
        add(MekanismLang.DESCRIPTION_CHEMICAL_TANK, "A portable tank that lets you carry chemicals wherever you please.");
        add(MekanismLang.DESCRIPTION_DIVERSION, "- Controllable by redstone");
        add(MekanismLang.DESCRIPTION_RESTRICTIVE, "- Only used if no other paths available");
        add(MekanismLang.DESCRIPTION_SPS_CASING, "Reinforced casing capable of resisting intense chemical and thermal effects from phase-shifting reactions.");
        add(MekanismLang.DESCRIPTION_SPS_PORT, "A port used for the transfer of energy and substances in the Supercritical Phase Shifter.");
        add(MekanismLang.DESCRIPTION_SUPERCHARGED_COIL, "Used in Supercritical Phase Shifter multiblock to supply large quantities of energy. Must be attached to a SPS Port.");
        // Modules
        add(MekanismLang.MODULE_ENABLED, "Enabled");
        add(MekanismLang.MODULE_ENABLED_LOWER, "enabled");
        add(MekanismLang.MODULE_DISABLED_LOWER, "disabled");
        add(MekanismLang.MODULE_DAMAGE, "Damage: %s");
        add(MekanismLang.MODULE_TWEAKER, "Module Tweaker");
        add(MekanismLang.MODULE_INSTALLED, "Installed: %s");
        add(MekanismLang.MODULE_STACKABLE, "Stackable: %s");
        add(MekanismLang.MODULE_EXCLUSIVE, "(Exclusive Module)");
        add(MekanismLang.MODULE_HANDLE_MODE_CHANGE, "Handle Mode Key");
        add(MekanismLang.MODULE_RENDER_HUD, "Show in HUD");
        add(MekanismLang.MODULE_MODE, "Mode");
        add(MekanismLang.MODULE_ATTACK_DAMAGE, "Attack Damage");
        add(MekanismLang.MODULE_FARMING_RADIUS, "Farming Radius");
        add(MekanismLang.MODULE_JUMP_BOOST, "Jump Boost");
        add(MekanismLang.MODULE_STEP_ASSIST, "Step Assist");
        add(MekanismLang.MODULE_RANGE, "Range");
        add(MekanismLang.MODULE_SPRINT_BOOST, "Sprint Boost");
        add(MekanismLang.MODULE_EXTENDED_MODE, "Extended Mode");
        add(MekanismLang.MODULE_EXTENDED_ENABLED, "Extended Vein Mining: %s");
        add(MekanismLang.MODULE_EXCAVATION_RANGE, "Excavation Range");
        add(MekanismLang.MODULE_EFFICIENCY, "Efficiency");
        add(MekanismLang.MODULE_MODE_CHANGE, "%s bumped to: %s");
        add(MekanismLang.MODULE_JETPACK_MODE, "Jetpack Mode");
        add(MekanismLang.MODULE_GRAVITATIONAL_MODULATION, "Gravitational Modulation");
        add(MekanismLang.MODULE_CHARGE_SUIT, "Charge Suit");
        add(MekanismLang.MODULE_CHARGE_INVENTORY, "Charge Inventory");
        add(MekanismLang.MODULE_SPEED_BOOST, "Speed Boost");
        add(MekanismLang.MODULE_VISION_ENHANCEMENT, "Vision Enhancement");
        add(MekanismLang.MODULE_PURIFICATION_BENEFICIAL, "Remove Beneficial");
        add(MekanismLang.MODULE_PURIFICATION_NEUTRAL, "Remove Neutral");
        add(MekanismLang.MODULE_PURIFICATION_HARMFUL, "Remove Harmful");

        add(MekanismLang.MODULE_ENERGY_UNIT, "Energy Unit");

        add(MekanismLang.MODULE_EXCAVATION_ESCALATION_UNIT, "Excavation Escalation Unit");
        add(MekanismLang.MODULE_ATTACK_AMPLIFICATION_UNIT, "Attack Amplification Unit");
        add(MekanismLang.MODULE_SILK_TOUCH_UNIT, "Silk Touch Unit");
        add(MekanismLang.MODULE_VEIN_MINING_UNIT, "Vein Mining Unit");
        add(MekanismLang.MODULE_FARMING_UNIT, "Farming Unit");
        add(MekanismLang.MODULE_TELEPORTATION_UNIT, "Teleportation Unit");

        add(MekanismLang.MODULE_ELECTROLYTIC_BREATHING_UNIT, "Electrolytic Breathing Unit");
        add(MekanismLang.MODULE_INHALATION_PURIFICATION_UNIT, "Inhalation Purification Unit");
        add(MekanismLang.MODULE_VISION_ENHANCEMENT_UNIT, "Vision Enhancement Unit");
        add(MekanismLang.MODULE_SOLAR_RECHARGING_UNIT, "Solar Recharging Unit");
        add(MekanismLang.MODULE_NUTRITIONAL_INJECTION_UNIT, "Nutritional Injection Unit");
        add(MekanismLang.MODULE_RADIATION_SHIELDING_UNIT, "Radiation Shielding Unit");
        add(MekanismLang.MODULE_JETPACK_UNIT, "Jetpack Unit");
        add(MekanismLang.MODULE_GRAVITATIONAL_MODULATING_UNIT, "Gravitational Modulating Unit");
        add(MekanismLang.MODULE_CHARGE_DISTRIBUTION_UNIT, "Charge Distribution Unit");
        add(MekanismLang.MODULE_DOSIMETER_UNIT, "Dosimeter Unit");
        add(MekanismLang.MODULE_LOCOMOTIVE_BOOSTING_UNIT, "Locomotive Boosting Unit");
        add(MekanismLang.MODULE_HYDRAULIC_PROPULSION_UNIT, "Hydraulic Propulsion Unit");
        add(MekanismLang.MODULE_MAGNETIC_ATTRACTION_UNIT, "Magnetic Attraction Unit");

        add(MekanismLang.DESCRIPTION_ENERGY_UNIT, "Increases maximum energy capacity.");

        add(MekanismLang.DESCRIPTION_EXCAVATION_ESCALATION_UNIT, "Increases digging speed on any block.");
        add(MekanismLang.DESCRIPTION_ATTACK_AMPLIFICATION_UNIT, "Amplifies melee attacks on players or mobs.");
        add(MekanismLang.DESCRIPTION_SILK_TOUCH_UNIT, "Allows all mined blocks to drop as themselves.");
        add(MekanismLang.DESCRIPTION_VEIN_MINING_UNIT, "Allows for quick mining of ore deposits and rapid felling of trees.");
        add(MekanismLang.DESCRIPTION_FARMING_UNIT, "Allows for soil tilling, log stripping, and soil flattening.");
        add(MekanismLang.DESCRIPTION_TELEPORTATION_UNIT, "Provides for quick travel to nearby blocks.");

        add(MekanismLang.DESCRIPTION_ELECTROLYTIC_BREATHING_UNIT, "Uses electrolysis to create breathable oxygen from water. Will also fill a jetpack module with hydrogen when necessary.");
        add(MekanismLang.DESCRIPTION_INHALATION_PURIFICATION_UNIT, "Applies a miniature electromagnetic field around the breathing apparatus, preventing selected potion effect types.");
        add(MekanismLang.DESCRIPTION_RADIATION_SHIELDING_UNIT, "Provides thick, radiation-proof metal plating to any MekaSuit armor piece.");
        add(MekanismLang.DESCRIPTION_VISION_ENHANCEMENT_UNIT, "Brightens the surrounding environment, allowing the user to see through darkness. Install multiple for more effective night vision.");
        add(MekanismLang.DESCRIPTION_SOLAR_RECHARGING_UNIT, "Harnesses the power of the sun to charge your MekaSuit. Install multiple for faster charging. Requires Mekanism: Generators.");
        add(MekanismLang.DESCRIPTION_NUTRITIONAL_INJECTION_UNIT, "Automatically feeds the player Nutritional Paste when hungry.");
        add(MekanismLang.DESCRIPTION_JETPACK_UNIT, "Applies a hydrogen-fueled jetpack to the MekaSuit.");
        add(MekanismLang.DESCRIPTION_GRAVITATIONAL_MODULATING_UNIT, "Using experimental technologies and the tremendous energy of antimatter, allows the user to defy gravity.");
        add(MekanismLang.DESCRIPTION_CHARGE_DISTRIBUTION_UNIT, "Evenly distributes charge throughout all worn MekaSuit armor.");
        add(MekanismLang.DESCRIPTION_DOSIMETER_UNIT, "Displays the user's current radiation dose in the HUD.");
        add(MekanismLang.DESCRIPTION_LOCOMOTIVE_BOOSTING_UNIT, "Increases the user's sprinting speed (and jumping distance).");
        add(MekanismLang.DESCRIPTION_HYDRAULIC_PROPULSION_UNIT, "Allows the user to both step and jump higher.");
        add(MekanismLang.DESCRIPTION_MAGNETIC_ATTRACTION_UNIT, "Uses powerful magnets to draw distant items towards the player. Install multiple for a greater range.");

        add("description.mekanism.osmium_ore", "A strong mineral that can be found at nearly any height in the world. It is known to have many uses in the construction of machinery.");
        add("description.mekanism.copper_ore", "A common, conductive material that can be used in the production of wires. Its ability to withstand high heats also makes it essential to advanced machinery.");
        add("description.mekanism.tin_ore", "A lightweight, yet sturdy, conductive material that is found slightly less commonly than Copper.");
        add("description.mekanism.fluorite_ore", "A mineral found relatively deep under the world's surface. The crystals can be processed into Hydrofluoric Acid, an essential chemical for Uranium processing.");
        add("description.mekanism.uranium_ore", "A common, heavy metal, which can yield massive amounts of energy when properly processed. In its naturally-occurring form, it is not radioactive enough to cause harm.");
        add("description.mekanism.lead_ore", "A somewhat rare metal that is excellent at resisting radioactive particles, spawning slightly less frequently than iron.");
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

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }
}
