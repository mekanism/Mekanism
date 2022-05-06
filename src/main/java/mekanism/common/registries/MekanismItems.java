package mekanism.common.registries;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemAlloy;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.item.ItemDictionary;
import mekanism.common.item.ItemDosimeter;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.item.ItemGeigerCounter;
import mekanism.common.item.ItemModule;
import mekanism.common.item.ItemNetworkReader;
import mekanism.common.item.ItemPortableQIODashboard;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemQIODrive;
import mekanism.common.item.ItemRefinedGlowstoneIngot;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.item.gear.ItemArmoredJetpack;
import mekanism.common.item.gear.ItemAtomicDisassembler;
import mekanism.common.item.gear.ItemCanteen;
import mekanism.common.item.gear.ItemElectricBow;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemHDPEElytra;
import mekanism.common.item.gear.ItemHazmatSuitArmor;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.item.gear.ItemScubaMask;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.resource.IResource;
import mekanism.common.resource.MiscResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tier.QIODriveTier;
import mekanism.common.util.EnumUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class MekanismItems {

    private MekanismItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(Mekanism.MODID);
    public static final Table<ResourceType, PrimaryResource, ItemRegistryObject<Item>> PROCESSED_RESOURCES = HashBasedTable.create();

    public static final ItemRegistryObject<ItemRobit> ROBIT = ITEMS.register("robit", ItemRobit::new);
    public static final ItemRegistryObject<ItemEnergized> ENERGY_TABLET = ITEMS.register("energy_tablet", () -> new ItemEnergized(MekanismConfig.gear.tabletChargeRate, MekanismConfig.gear.tabletMaxEnergy, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, ItemDeferredRegister.getMekBaseProperties().rarity(Rarity.UNCOMMON)));
    public static final ItemRegistryObject<ItemConfigurator> CONFIGURATOR = ITEMS.register("configurator", ItemConfigurator::new);
    public static final ItemRegistryObject<ItemNetworkReader> NETWORK_READER = ITEMS.register("network_reader", ItemNetworkReader::new);
    public static final ItemRegistryObject<ItemDictionary> DICTIONARY = ITEMS.register("dictionary", ItemDictionary::new);
    public static final ItemRegistryObject<ItemPortableTeleporter> PORTABLE_TELEPORTER = ITEMS.register("portable_teleporter", ItemPortableTeleporter::new);
    public static final ItemRegistryObject<ItemConfigurationCard> CONFIGURATION_CARD = ITEMS.register("configuration_card", ItemConfigurationCard::new);
    public static final ItemRegistryObject<ItemCraftingFormula> CRAFTING_FORMULA = ITEMS.register("crafting_formula", ItemCraftingFormula::new);
    public static final ItemRegistryObject<ItemSeismicReader> SEISMIC_READER = ITEMS.register("seismic_reader", ItemSeismicReader::new);
    public static final ItemRegistryObject<ItemGaugeDropper> GAUGE_DROPPER = ITEMS.register("gauge_dropper", ItemGaugeDropper::new);
    public static final ItemRegistryObject<ItemGeigerCounter> GEIGER_COUNTER = ITEMS.register("geiger_counter", ItemGeigerCounter::new);
    public static final ItemRegistryObject<ItemDosimeter> DOSIMETER = ITEMS.register("dosimeter", ItemDosimeter::new);
    public static final ItemRegistryObject<ItemCanteen> CANTEEN = ITEMS.register("canteen", ItemCanteen::new);
    public static final ItemRegistryObject<ItemPortableQIODashboard> PORTABLE_QIO_DASHBOARD = ITEMS.register("portable_qio_dashboard", ItemPortableQIODashboard::new);
    // QIO Drives
    public static final ItemRegistryObject<ItemQIODrive> BASE_QIO_DRIVE = registerQIODrive(QIODriveTier.BASE);
    public static final ItemRegistryObject<ItemQIODrive> HYPER_DENSE_QIO_DRIVE = registerQIODrive(QIODriveTier.HYPER_DENSE);
    public static final ItemRegistryObject<ItemQIODrive> TIME_DILATING_QIO_DRIVE = registerQIODrive(QIODriveTier.TIME_DILATING);
    public static final ItemRegistryObject<ItemQIODrive> SUPERMASSIVE_QIO_DRIVE = registerQIODrive(QIODriveTier.SUPERMASSIVE);
    // Tools
    public static final ItemRegistryObject<ItemAtomicDisassembler> ATOMIC_DISASSEMBLER = ITEMS.register("atomic_disassembler", ItemAtomicDisassembler::new);
    public static final ItemRegistryObject<ItemElectricBow> ELECTRIC_BOW = ITEMS.register("electric_bow", ItemElectricBow::new);
    public static final ItemRegistryObject<ItemFlamethrower> FLAMETHROWER = ITEMS.register("flamethrower", ItemFlamethrower::new);
    public static final ItemRegistryObject<ItemMekaTool> MEKA_TOOL = ITEMS.registerUnburnable("meka_tool", ItemMekaTool::new);
    // Armor
    public static final ItemRegistryObject<ItemFreeRunners> FREE_RUNNERS = ITEMS.register("free_runners", ItemFreeRunners::new);
    public static final ItemRegistryObject<ItemScubaMask> SCUBA_MASK = ITEMS.register("scuba_mask", ItemScubaMask::new);
    public static final ItemRegistryObject<ItemScubaTank> SCUBA_TANK = ITEMS.register("scuba_tank", ItemScubaTank::new);
    public static final ItemRegistryObject<ItemJetpack> JETPACK = ITEMS.register("jetpack", ItemJetpack::new);
    public static final ItemRegistryObject<ItemArmoredJetpack> ARMORED_JETPACK = ITEMS.register("jetpack_armored", ItemArmoredJetpack::new);
    public static final ItemRegistryObject<ItemHDPEElytra> HDPE_REINFORCED_ELYTRA = ITEMS.register("hdpe_elytra", props -> new ItemHDPEElytra(props.durability(648).rarity(Rarity.RARE)));

    public static final ItemRegistryObject<ItemHazmatSuitArmor> HAZMAT_MASK = ITEMS.register("hazmat_mask", props -> new ItemHazmatSuitArmor(EquipmentSlot.HEAD, props));
    public static final ItemRegistryObject<ItemHazmatSuitArmor> HAZMAT_GOWN = ITEMS.register("hazmat_gown", props -> new ItemHazmatSuitArmor(EquipmentSlot.CHEST, props));
    public static final ItemRegistryObject<ItemHazmatSuitArmor> HAZMAT_PANTS = ITEMS.register("hazmat_pants", props -> new ItemHazmatSuitArmor(EquipmentSlot.LEGS, props));
    public static final ItemRegistryObject<ItemHazmatSuitArmor> HAZMAT_BOOTS = ITEMS.register("hazmat_boots", props -> new ItemHazmatSuitArmor(EquipmentSlot.FEET, props));

    public static final ItemRegistryObject<ItemMekaSuitArmor> MEKASUIT_HELMET = ITEMS.registerUnburnable("mekasuit_helmet", props -> new ItemMekaSuitArmor(EquipmentSlot.HEAD, props));
    public static final ItemRegistryObject<ItemMekaSuitArmor> MEKASUIT_BODYARMOR = ITEMS.registerUnburnable("mekasuit_bodyarmor", props -> new ItemMekaSuitArmor(EquipmentSlot.CHEST, props));
    public static final ItemRegistryObject<ItemMekaSuitArmor> MEKASUIT_PANTS = ITEMS.registerUnburnable("mekasuit_pants", props -> new ItemMekaSuitArmor(EquipmentSlot.LEGS, props));
    public static final ItemRegistryObject<ItemMekaSuitArmor> MEKASUIT_BOOTS = ITEMS.registerUnburnable("mekasuit_boots", props -> new ItemMekaSuitArmor(EquipmentSlot.FEET, props));

    public static final ItemRegistryObject<Item> MODULE_BASE = ITEMS.register("module_base");

    public static final ItemRegistryObject<ItemModule> MODULE_ENERGY = ITEMS.registerModule(MekanismModules.ENERGY_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_LASER_DISSIPATION = ITEMS.registerModule(MekanismModules.LASER_DISSIPATION_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_RADIATION_SHIELDING = ITEMS.registerModule(MekanismModules.RADIATION_SHIELDING_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_EXCAVATION_ESCALATION = ITEMS.registerModule(MekanismModules.EXCAVATION_ESCALATION_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_ATTACK_AMPLIFICATION = ITEMS.registerModule(MekanismModules.ATTACK_AMPLIFICATION_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_FARMING = ITEMS.registerModule(MekanismModules.FARMING_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_SHEARING = ITEMS.registerModule(MekanismModules.SHEARING_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_SILK_TOUCH = ITEMS.registerModule(MekanismModules.SILK_TOUCH_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_VEIN_MINING = ITEMS.registerModule(MekanismModules.VEIN_MINING_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_TELEPORTATION = ITEMS.registerModule(MekanismModules.TELEPORTATION_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_ELECTROLYTIC_BREATHING = ITEMS.registerModule(MekanismModules.ELECTROLYTIC_BREATHING_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_INHALATION_PURIFICATION = ITEMS.registerModule(MekanismModules.INHALATION_PURIFICATION_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_VISION_ENHANCEMENT = ITEMS.registerModule(MekanismModules.VISION_ENHANCEMENT_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_NUTRITIONAL_INJECTION = ITEMS.registerModule(MekanismModules.NUTRITIONAL_INJECTION_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_DOSIMETER = ITEMS.registerModule(MekanismModules.DOSIMETER_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_GEIGER = ITEMS.registerModule(MekanismModules.GEIGER_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_JETPACK = ITEMS.registerModule(MekanismModules.JETPACK_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_CHARGE_DISTRIBUTION = ITEMS.registerModule(MekanismModules.CHARGE_DISTRIBUTION_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_GRAVITATIONAL_MODULATING = ITEMS.registerModule(MekanismModules.GRAVITATIONAL_MODULATING_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_ELYTRA = ITEMS.registerModule(MekanismModules.ELYTRA_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_GYROSCOPIC_STABILIZATION = ITEMS.registerModule(MekanismModules.GYROSCOPIC_STABILIZATION_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_LOCOMOTIVE_BOOSTING = ITEMS.registerModule(MekanismModules.LOCOMOTIVE_BOOSTING_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_HYDRAULIC_PROPULSION = ITEMS.registerModule(MekanismModules.HYDRAULIC_PROPULSION_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_MAGNETIC_ATTRACTION = ITEMS.registerModule(MekanismModules.MAGNETIC_ATTRACTION_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_FROST_WALKER = ITEMS.registerModule(MekanismModules.FROST_WALKER_UNIT);

    public static final ItemRegistryObject<ItemUpgrade> SPEED_UPGRADE = registerUpgrade(Upgrade.SPEED);
    public static final ItemRegistryObject<ItemUpgrade> ENERGY_UPGRADE = registerUpgrade(Upgrade.ENERGY);
    public static final ItemRegistryObject<ItemUpgrade> FILTER_UPGRADE = registerUpgrade(Upgrade.FILTER);
    public static final ItemRegistryObject<ItemUpgrade> MUFFLING_UPGRADE = registerUpgrade(Upgrade.MUFFLING);
    public static final ItemRegistryObject<ItemUpgrade> GAS_UPGRADE = registerUpgrade(Upgrade.GAS);
    public static final ItemRegistryObject<ItemUpgrade> ANCHOR_UPGRADE = registerUpgrade(Upgrade.ANCHOR);
    public static final ItemRegistryObject<ItemUpgrade> STONE_GENERATOR_UPGRADE = registerUpgrade(Upgrade.STONE_GENERATOR);

    public static final ItemRegistryObject<ItemTierInstaller> BASIC_TIER_INSTALLER = registerInstaller(null, BaseTier.BASIC);
    public static final ItemRegistryObject<ItemTierInstaller> ADVANCED_TIER_INSTALLER = registerInstaller(BaseTier.BASIC, BaseTier.ADVANCED);
    public static final ItemRegistryObject<ItemTierInstaller> ELITE_TIER_INSTALLER = registerInstaller(BaseTier.ADVANCED, BaseTier.ELITE);
    public static final ItemRegistryObject<ItemTierInstaller> ULTIMATE_TIER_INSTALLER = registerInstaller(BaseTier.ELITE, BaseTier.ULTIMATE);

    public static final ItemRegistryObject<Item> BASIC_CONTROL_CIRCUIT = registerCircuit(BaseTier.BASIC);
    public static final ItemRegistryObject<Item> ADVANCED_CONTROL_CIRCUIT = registerCircuit(BaseTier.ADVANCED);
    public static final ItemRegistryObject<Item> ELITE_CONTROL_CIRCUIT = registerCircuit(BaseTier.ELITE);
    public static final ItemRegistryObject<Item> ULTIMATE_CONTROL_CIRCUIT = registerCircuit(BaseTier.ULTIMATE);

    //Alloy names are alloy_type for purposes of tab complete
    public static final ItemRegistryObject<ItemAlloy> INFUSED_ALLOY = registerAlloy(AlloyTier.INFUSED, Rarity.UNCOMMON);
    public static final ItemRegistryObject<ItemAlloy> REINFORCED_ALLOY = registerAlloy(AlloyTier.REINFORCED, Rarity.RARE);
    public static final ItemRegistryObject<ItemAlloy> ATOMIC_ALLOY = registerAlloy(AlloyTier.ATOMIC, Rarity.EPIC);

    public static final ItemRegistryObject<Item> ENRICHED_CARBON = registerResource(ResourceType.ENRICHED, MiscResource.CARBON);
    public static final ItemRegistryObject<Item> ENRICHED_REDSTONE = registerResource(ResourceType.ENRICHED, MiscResource.REDSTONE);
    public static final ItemRegistryObject<Item> ENRICHED_DIAMOND = registerResource(ResourceType.ENRICHED, MiscResource.DIAMOND);
    public static final ItemRegistryObject<Item> ENRICHED_OBSIDIAN = registerResource(ResourceType.ENRICHED, MiscResource.REFINED_OBSIDIAN);
    public static final ItemRegistryObject<Item> ENRICHED_GOLD = registerResource(ResourceType.ENRICHED, PrimaryResource.GOLD);
    public static final ItemRegistryObject<Item> ENRICHED_TIN = registerResource(ResourceType.ENRICHED, PrimaryResource.TIN);

    public static final ItemRegistryObject<Item> HDPE_PELLET = ITEMS.register("hdpe_pellet", Rarity.UNCOMMON);
    public static final ItemRegistryObject<Item> HDPE_ROD = ITEMS.register("hdpe_rod", Rarity.UNCOMMON);
    public static final ItemRegistryObject<Item> HDPE_SHEET = ITEMS.register("hdpe_sheet", Rarity.UNCOMMON);
    public static final ItemRegistryObject<Item> HDPE_STICK = ITEMS.register("hdpe_stick", Rarity.UNCOMMON);

    public static final ItemRegistryObject<Item> ELECTROLYTIC_CORE = ITEMS.register("electrolytic_core", Rarity.UNCOMMON);
    public static final ItemRegistryObject<Item> TELEPORTATION_CORE = ITEMS.register("teleportation_core", Rarity.RARE);
    public static final ItemRegistryObject<Item> ANTIMATTER_PELLET = ITEMS.register("pellet_antimatter", EnumColor.PURPLE);
    public static final ItemRegistryObject<Item> PLUTONIUM_PELLET = ITEMS.register("pellet_plutonium", EnumColor.GRAY);
    public static final ItemRegistryObject<Item> POLONIUM_PELLET = ITEMS.register("pellet_polonium", EnumColor.INDIGO);
    public static final ItemRegistryObject<Item> REPROCESSED_FISSILE_FRAGMENT = ITEMS.register("reprocessed_fissile_fragment", Rarity.RARE);

    public static final ItemRegistryObject<Item> ENRICHED_IRON = ITEMS.register("enriched_iron");
    public static final ItemRegistryObject<Item> SAWDUST = ITEMS.register("sawdust");
    public static final ItemRegistryObject<Item> SALT = ITEMS.register("salt");
    public static final ItemRegistryObject<Item> SUBSTRATE = ITEMS.register("substrate");
    public static final ItemRegistryObject<Item> BIO_FUEL = ITEMS.register("bio_fuel");
    public static final ItemRegistryObject<Item> DYE_BASE = ITEMS.register("dye_base");
    public static final ItemRegistryObject<Item> FLUORITE_GEM = ITEMS.register("fluorite_gem");
    public static final ItemRegistryObject<Item> YELLOW_CAKE_URANIUM = ITEMS.register("yellow_cake_uranium", Rarity.UNCOMMON);
    public static final ItemRegistryObject<Item> DIRTY_NETHERITE_SCRAP = ITEMS.registerUnburnable("dirty_netherite_scrap");

    public static final ItemRegistryObject<Item> BRONZE_DUST = registerResource(ResourceType.DUST, MiscResource.BRONZE);
    public static final ItemRegistryObject<Item> LAPIS_LAZULI_DUST = registerResource(ResourceType.DUST, MiscResource.LAPIS_LAZULI);
    public static final ItemRegistryObject<Item> COAL_DUST = registerResource(ResourceType.DUST, MiscResource.COAL);
    public static final ItemRegistryObject<Item> CHARCOAL_DUST = registerResource(ResourceType.DUST, MiscResource.CHARCOAL);
    public static final ItemRegistryObject<Item> QUARTZ_DUST = registerResource(ResourceType.DUST, MiscResource.QUARTZ);
    public static final ItemRegistryObject<Item> EMERALD_DUST = registerResource(ResourceType.DUST, MiscResource.EMERALD);
    public static final ItemRegistryObject<Item> DIAMOND_DUST = registerResource(ResourceType.DUST, MiscResource.DIAMOND);
    public static final ItemRegistryObject<Item> NETHERITE_DUST = registerResource(ResourceType.DUST, MiscResource.NETHERITE);
    public static final ItemRegistryObject<Item> STEEL_DUST = registerResource(ResourceType.DUST, MiscResource.STEEL);
    public static final ItemRegistryObject<Item> SULFUR_DUST = registerResource(ResourceType.DUST, MiscResource.SULFUR);
    public static final ItemRegistryObject<Item> LITHIUM_DUST = registerResource(ResourceType.DUST, MiscResource.LITHIUM);
    public static final ItemRegistryObject<Item> REFINED_OBSIDIAN_DUST = registerResource(ResourceType.DUST, MiscResource.REFINED_OBSIDIAN);
    public static final ItemRegistryObject<Item> OBSIDIAN_DUST = registerResource(ResourceType.DUST, MiscResource.OBSIDIAN);
    public static final ItemRegistryObject<Item> FLUORITE_DUST = registerResource(ResourceType.DUST, MiscResource.FLUORITE);

    public static final ItemRegistryObject<Item> BRONZE_INGOT = registerResource(ResourceType.INGOT, MiscResource.BRONZE);
    public static final ItemRegistryObject<Item> REFINED_OBSIDIAN_INGOT = registerUnburnableResource(ResourceType.INGOT, MiscResource.REFINED_OBSIDIAN);
    public static final ItemRegistryObject<Item> REFINED_GLOWSTONE_INGOT = ITEMS.register(ResourceType.INGOT.getRegistryPrefix() + "_" + MiscResource.REFINED_GLOWSTONE.getRegistrySuffix(), ItemRefinedGlowstoneIngot::new);
    public static final ItemRegistryObject<Item> STEEL_INGOT = registerResource(ResourceType.INGOT, MiscResource.STEEL);

    public static final ItemRegistryObject<Item> REFINED_OBSIDIAN_NUGGET = registerUnburnableResource(ResourceType.NUGGET, MiscResource.REFINED_OBSIDIAN);
    public static final ItemRegistryObject<Item> BRONZE_NUGGET = registerResource(ResourceType.NUGGET, MiscResource.BRONZE);
    public static final ItemRegistryObject<Item> REFINED_GLOWSTONE_NUGGET = registerResource(ResourceType.NUGGET, MiscResource.REFINED_GLOWSTONE);
    public static final ItemRegistryObject<Item> STEEL_NUGGET = registerResource(ResourceType.NUGGET, MiscResource.STEEL);

    static {
        for (ResourceType type : EnumUtils.RESOURCE_TYPES) {
            for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
                if (resource.has(type)) {
                    PROCESSED_RESOURCES.put(type, resource, registerResource(type, resource));
                }
            }
        }
    }

    private static ItemRegistryObject<Item> registerResource(ResourceType type, IResource resource) {
        return ITEMS.register(type.getRegistryPrefix() + "_" + resource.getRegistrySuffix());
    }

    private static ItemRegistryObject<Item> registerUnburnableResource(ResourceType type, IResource resource) {
        return ITEMS.registerUnburnable(type.getRegistryPrefix() + "_" + resource.getRegistrySuffix());
    }

    private static ItemRegistryObject<Item> registerCircuit(BaseTier tier) {
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        return ITEMS.register(tier.getLowerName() + "_control_circuit", tier.getTextColor());
    }

    private static ItemRegistryObject<ItemTierInstaller> registerInstaller(@Nullable BaseTier fromTier, @Nonnull BaseTier toTier) {
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        return ITEMS.register(toTier.getLowerName() + "_tier_installer", properties -> new ItemTierInstaller(fromTier, toTier, properties));
    }

    private static ItemRegistryObject<ItemAlloy> registerAlloy(AlloyTier tier, Rarity rarity) {
        return ITEMS.register("alloy_" + tier.getName(), properties -> new ItemAlloy(tier, properties.rarity(rarity)));
    }

    private static ItemRegistryObject<ItemUpgrade> registerUpgrade(Upgrade type) {
        return ITEMS.register("upgrade_" + type.getRawName(), properties -> new ItemUpgrade(type, properties));
    }

    private static ItemRegistryObject<ItemQIODrive> registerQIODrive(QIODriveTier tier) {
        return ITEMS.register("qio_drive_" + tier.name().toLowerCase(Locale.ROOT), properties -> new ItemQIODrive(tier, properties));
    }
}