package mekanism.common;

import java.util.Locale;
import mekanism.api.Upgrade;
import mekanism.common.item.ItemAlloy;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.item.ItemDictionary;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.item.ItemNetworkReader;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemProxy;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.item.gear.ItemArmoredJetpack;
import mekanism.common.item.gear.ItemAtomicDisassembler;
import mekanism.common.item.gear.ItemElectricBow;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemGasMask;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.resource.INamedResource;
import mekanism.common.resource.MiscResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tier.AlloyTier;
import mekanism.common.tier.BaseTier;
import net.minecraft.item.Item;

public class MekanismItem {

    public static ItemDeferredRegister ITEMS = new ItemDeferredRegister(Mekanism.MODID);

    public static final ItemRegistryObject<ItemProxy> ITEM_PROXY = ITEMS.register("item_proxy", ItemProxy::new);

    public static final ItemRegistryObject<ItemElectricBow> ELECTRIC_BOW = ITEMS.register("electric_bow", ItemElectricBow::new);
    public static final ItemRegistryObject<ItemRobit> ROBIT = ITEMS.register("robit", ItemRobit::new);
    public static final ItemRegistryObject<ItemAtomicDisassembler> ATOMIC_DISASSEMBLER = ITEMS.register("atomic_disassembler", ItemAtomicDisassembler::new);
    public static final ItemRegistryObject<ItemEnergized> ENERGY_TABLET = ITEMS.register("energy_tablet", () -> new ItemEnergized(1_000_000));
    public static final ItemRegistryObject<ItemConfigurator> CONFIGURATOR = ITEMS.register("configurator", ItemConfigurator::new);
    public static final ItemRegistryObject<ItemNetworkReader> NETWORK_READER = ITEMS.register("network_reader", ItemNetworkReader::new);
    public static final ItemRegistryObject<ItemDictionary> DICTIONARY = ITEMS.register("dictionary", ItemDictionary::new);
    public static final ItemRegistryObject<ItemGasMask> GAS_MASK = ITEMS.register("gas_mask", ItemGasMask::new);
    public static final ItemRegistryObject<ItemScubaTank> SCUBA_TANK = ITEMS.register("scuba_tank", ItemScubaTank::new);
    public static final ItemRegistryObject<ItemPortableTeleporter> PORTABLE_TELEPORTER = ITEMS.register("portable_teleporter", ItemPortableTeleporter::new);
    public static final ItemRegistryObject<ItemFreeRunners> FREE_RUNNERS = ITEMS.register("free_runners", ItemFreeRunners::new);
    public static final ItemRegistryObject<ItemConfigurationCard> CONFIGURATION_CARD = ITEMS.register("configuration_card", ItemConfigurationCard::new);
    public static final ItemRegistryObject<ItemCraftingFormula> CRAFTING_FORMULA = ITEMS.register("crafting_formula", ItemCraftingFormula::new);
    public static final ItemRegistryObject<ItemSeismicReader> SEISMIC_READER = ITEMS.register("seismic_reader", ItemSeismicReader::new);
    public static final ItemRegistryObject<ItemFlamethrower> FLAMETHROWER = ITEMS.register("flamethrower", ItemFlamethrower::new);
    public static final ItemRegistryObject<ItemGaugeDropper> GAUGE_DROPPER = ITEMS.register("gauge_dropper", ItemGaugeDropper::new);
    public static final ItemRegistryObject<Item> TELEPORTATION_CORE = ITEMS.register("teleportation_core");
    //TODO: Rename enriched iron?
    public static final ItemRegistryObject<Item> ENRICHED_IRON = ITEMS.register("enriched_iron");
    public static final ItemRegistryObject<Item> ELECTROLYTIC_CORE = ITEMS.register("electrolytic_core");
    //TODO: pulps/wood? Doesn't really make sense so not adding it unless it is one of the standards
    public static final ItemRegistryObject<Item> SAWDUST = ITEMS.register("sawdust");
    //TODO: Once more mods are on 1.14 see what standard for salt is, if we should have forge:foods/salt etc
    public static final ItemRegistryObject<Item> SALT = ITEMS.register("salt");
    public static final ItemRegistryObject<Item> SUBSTRATE = ITEMS.register("substrate");
    //TODO: Make sure we match the common spec
    public static final ItemRegistryObject<Item> BIO_FUEL = ITEMS.register("bio_fuel");

    //TODO: Should we make an enriched type for each infusion type
    public static final ItemRegistryObject<Item> ENRICHED_CARBON = registerResource(ResourceType.ENRICHED, MiscResource.CARBON);
    public static final ItemRegistryObject<Item> ENRICHED_REDSTONE = registerResource(ResourceType.ENRICHED, MiscResource.REDSTONE);
    public static final ItemRegistryObject<Item> ENRICHED_DIAMOND = registerResource(ResourceType.ENRICHED, MiscResource.DIAMOND);
    public static final ItemRegistryObject<Item> ENRICHED_OBSIDIAN = registerResource(ResourceType.ENRICHED, MiscResource.REFINED_OBSIDIAN);

    public static final ItemRegistryObject<ItemUpgrade> SPEED_UPGRADE = registerUpgrade(Upgrade.SPEED);
    public static final ItemRegistryObject<ItemUpgrade> ENERGY_UPGRADE = registerUpgrade(Upgrade.ENERGY);
    public static final ItemRegistryObject<ItemUpgrade> FILTER_UPGRADE = registerUpgrade(Upgrade.FILTER);
    public static final ItemRegistryObject<ItemUpgrade> MUFFLING_UPGRADE = registerUpgrade(Upgrade.MUFFLING);
    public static final ItemRegistryObject<ItemUpgrade> GAS_UPGRADE = registerUpgrade(Upgrade.GAS);
    public static final ItemRegistryObject<ItemUpgrade> ANCHOR_UPGRADE = registerUpgrade(Upgrade.ANCHOR);

    //Alloy names are alloy_type for purposes of tab complete
    public static final ItemRegistryObject<ItemAlloy> INFUSED_ALLOY = registerAlloy(AlloyTier.INFUSED);
    public static final ItemRegistryObject<ItemAlloy> REINFORCED_ALLOY = registerAlloy(AlloyTier.REINFORCED);
    public static final ItemRegistryObject<ItemAlloy> ATOMIC_ALLOY = registerAlloy(AlloyTier.ATOMIC);

    public static final ItemRegistryObject<Item> BASIC_CONTROL_CIRCUIT = registerCircuit(BaseTier.BASIC);
    public static final ItemRegistryObject<Item> ADVANCED_CONTROL_CIRCUIT = registerCircuit(BaseTier.ADVANCED);
    public static final ItemRegistryObject<Item> ELITE_CONTROL_CIRCUIT = registerCircuit(BaseTier.ELITE);
    public static final ItemRegistryObject<Item> ULTIMATE_CONTROL_CIRCUIT = registerCircuit(BaseTier.ULTIMATE);

    public static final ItemRegistryObject<ItemJetpack> JETPACK = ITEMS.register("jetpack", ItemJetpack::new);
    public static final ItemRegistryObject<ItemArmoredJetpack> ARMORED_JETPACK = ITEMS.register("jetpack_armored", ItemArmoredJetpack::new);

    public static final ItemRegistryObject<Item> HDPE_PELLET = ITEMS.register("hdpe_pellet");
    public static final ItemRegistryObject<Item> HDPE_ROD = ITEMS.register("hdpe_rod");
    public static final ItemRegistryObject<Item> HDPE_SHEET = ITEMS.register("hdpe_sheet");
    public static final ItemRegistryObject<Item> HDPE_STICK = ITEMS.register("hdpe_stick");

    public static final ItemRegistryObject<ItemTierInstaller> BASIC_TIER_INSTALLER = registerInstaller(BaseTier.BASIC);
    public static final ItemRegistryObject<ItemTierInstaller> ADVANCED_TIER_INSTALLER = registerInstaller(BaseTier.ADVANCED);
    public static final ItemRegistryObject<ItemTierInstaller> ELITE_TIER_INSTALLER = registerInstaller(BaseTier.ELITE);
    public static final ItemRegistryObject<ItemTierInstaller> ULTIMATE_TIER_INSTALLER = registerInstaller(BaseTier.ULTIMATE);

    public static final ItemRegistryObject<Item> IRON_CRYSTAL = registerResource(ResourceType.CRYSTAL, Resource.IRON);
    public static final ItemRegistryObject<Item> GOLD_CRYSTAL = registerResource(ResourceType.CRYSTAL, Resource.GOLD);
    public static final ItemRegistryObject<Item> OSMIUM_CRYSTAL = registerResource(ResourceType.CRYSTAL, Resource.OSMIUM);
    public static final ItemRegistryObject<Item> COPPER_CRYSTAL = registerResource(ResourceType.CRYSTAL, Resource.COPPER);
    public static final ItemRegistryObject<Item> TIN_CRYSTAL = registerResource(ResourceType.CRYSTAL, Resource.TIN);

    public static final ItemRegistryObject<Item> IRON_SHARD = registerResource(ResourceType.SHARD, Resource.IRON);
    public static final ItemRegistryObject<Item> GOLD_SHARD = registerResource(ResourceType.SHARD, Resource.GOLD);
    public static final ItemRegistryObject<Item> OSMIUM_SHARD = registerResource(ResourceType.SHARD, Resource.OSMIUM);
    public static final ItemRegistryObject<Item> COPPER_SHARD = registerResource(ResourceType.SHARD, Resource.COPPER);
    public static final ItemRegistryObject<Item> TIN_SHARD = registerResource(ResourceType.SHARD, Resource.TIN);

    public static final ItemRegistryObject<Item> IRON_CLUMP = registerResource(ResourceType.CLUMP, Resource.IRON);
    public static final ItemRegistryObject<Item> GOLD_CLUMP = registerResource(ResourceType.CLUMP, Resource.GOLD);
    public static final ItemRegistryObject<Item> OSMIUM_CLUMP = registerResource(ResourceType.CLUMP, Resource.OSMIUM);
    public static final ItemRegistryObject<Item> COPPER_CLUMP = registerResource(ResourceType.CLUMP, Resource.COPPER);
    public static final ItemRegistryObject<Item> TIN_CLUMP = registerResource(ResourceType.CLUMP, Resource.TIN);

    public static final ItemRegistryObject<Item> DIRTY_IRON_DUST = registerResource(ResourceType.DIRTY_DUST, Resource.IRON);
    public static final ItemRegistryObject<Item> DIRTY_GOLD_DUST = registerResource(ResourceType.DIRTY_DUST, Resource.GOLD);
    public static final ItemRegistryObject<Item> DIRTY_OSMIUM_DUST = registerResource(ResourceType.DIRTY_DUST, Resource.OSMIUM);
    public static final ItemRegistryObject<Item> DIRTY_COPPER_DUST = registerResource(ResourceType.DIRTY_DUST, Resource.COPPER);
    public static final ItemRegistryObject<Item> DIRTY_TIN_DUST = registerResource(ResourceType.DIRTY_DUST, Resource.TIN);

    public static final ItemRegistryObject<Item> IRON_DUST = registerResource(ResourceType.DUST, Resource.IRON);
    public static final ItemRegistryObject<Item> GOLD_DUST = registerResource(ResourceType.DUST, Resource.GOLD);
    public static final ItemRegistryObject<Item> OSMIUM_DUST = registerResource(ResourceType.DUST, Resource.OSMIUM);
    public static final ItemRegistryObject<Item> COPPER_DUST = registerResource(ResourceType.DUST, Resource.COPPER);
    public static final ItemRegistryObject<Item> TIN_DUST = registerResource(ResourceType.DUST, Resource.TIN);

    public static final ItemRegistryObject<Item> BRONZE_DUST = registerResource(ResourceType.DUST, MiscResource.BRONZE);
    public static final ItemRegistryObject<Item> LAPIS_LAZULI_DUST = registerResource(ResourceType.DUST, MiscResource.LAPIS_LAZULI);
    public static final ItemRegistryObject<Item> COAL_DUST = registerResource(ResourceType.DUST, MiscResource.COAL);
    public static final ItemRegistryObject<Item> CHARCOAL_DUST = registerResource(ResourceType.DUST, MiscResource.CHARCOAL);
    public static final ItemRegistryObject<Item> QUARTZ_DUST = registerResource(ResourceType.DUST, MiscResource.QUARTZ);
    public static final ItemRegistryObject<Item> EMERALD_DUST = registerResource(ResourceType.DUST, MiscResource.EMERALD);
    public static final ItemRegistryObject<Item> DIAMOND_DUST = registerResource(ResourceType.DUST, MiscResource.DIAMOND);
    public static final ItemRegistryObject<Item> STEEL_DUST = registerResource(ResourceType.DUST, MiscResource.STEEL);
    public static final ItemRegistryObject<Item> SULFUR_DUST = registerResource(ResourceType.DUST, MiscResource.SULFUR);
    public static final ItemRegistryObject<Item> LITHIUM_DUST = registerResource(ResourceType.DUST, MiscResource.LITHIUM);
    public static final ItemRegistryObject<Item> REFINED_OBSIDIAN_DUST = registerResource(ResourceType.DUST, MiscResource.REFINED_OBSIDIAN);
    public static final ItemRegistryObject<Item> OBSIDIAN_DUST = registerResource(ResourceType.DUST, MiscResource.OBSIDIAN);

    public static final ItemRegistryObject<Item> REFINED_OBSIDIAN_INGOT = registerResource(ResourceType.INGOT, MiscResource.REFINED_OBSIDIAN);
    public static final ItemRegistryObject<Item> OSMIUM_INGOT = registerResource(ResourceType.INGOT, Resource.OSMIUM);
    public static final ItemRegistryObject<Item> BRONZE_INGOT = registerResource(ResourceType.INGOT, MiscResource.BRONZE);
    public static final ItemRegistryObject<Item> REFINED_GLOWSTONE_INGOT = registerResource(ResourceType.INGOT, MiscResource.REFINED_GLOWSTONE);
    public static final ItemRegistryObject<Item> STEEL_INGOT = registerResource(ResourceType.INGOT, MiscResource.STEEL);
    public static final ItemRegistryObject<Item> COPPER_INGOT = registerResource(ResourceType.INGOT, Resource.COPPER);
    public static final ItemRegistryObject<Item> TIN_INGOT = registerResource(ResourceType.INGOT, Resource.TIN);

    public static final ItemRegistryObject<Item> REFINED_OBSIDIAN_NUGGET = registerResource(ResourceType.NUGGET, MiscResource.REFINED_OBSIDIAN);
    public static final ItemRegistryObject<Item> OSMIUM_NUGGET = registerResource(ResourceType.NUGGET, Resource.OSMIUM);
    public static final ItemRegistryObject<Item> BRONZE_NUGGET = registerResource(ResourceType.NUGGET, MiscResource.BRONZE);
    public static final ItemRegistryObject<Item> REFINED_GLOWSTONE_NUGGET = registerResource(ResourceType.NUGGET, MiscResource.REFINED_GLOWSTONE);
    public static final ItemRegistryObject<Item> STEEL_NUGGET = registerResource(ResourceType.NUGGET, MiscResource.STEEL);
    public static final ItemRegistryObject<Item> COPPER_NUGGET = registerResource(ResourceType.NUGGET, Resource.COPPER);
    public static final ItemRegistryObject<Item> TIN_NUGGET = registerResource(ResourceType.NUGGET, Resource.TIN);

    private static ItemRegistryObject<Item> registerResource(ResourceType type, INamedResource resource) {
        return ITEMS.register(type.getRegistryPrefix() + "_" + resource.getRegistrySuffix());
    }

    private static ItemRegistryObject<Item> registerCircuit(BaseTier tier) {
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        return ITEMS.register(tier.getSimpleName().toLowerCase(Locale.ROOT) + "_control_circuit");
    }

    private static ItemRegistryObject<ItemTierInstaller> registerInstaller(BaseTier tier) {
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        return ITEMS.register(tier.getSimpleName().toLowerCase(Locale.ROOT) + "_tier_installer", properties -> new ItemTierInstaller(tier, properties));
    }

    private static ItemRegistryObject<ItemAlloy> registerAlloy(AlloyTier tier) {
        return ITEMS.register("alloy_" + tier.getName(), properties -> new ItemAlloy(tier, properties));
    }

    private static ItemRegistryObject<ItemUpgrade> registerUpgrade(Upgrade type) {
        return ITEMS.register("upgrade_" + type.getRawName(), properties -> new ItemUpgrade(type, properties));
    }
}