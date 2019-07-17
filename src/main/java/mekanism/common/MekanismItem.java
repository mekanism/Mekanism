package mekanism.common;

import mekanism.api.EnumColor;
import mekanism.common.item.ItemAlloy;
import mekanism.common.item.ItemArmoredJetpack;
import mekanism.common.item.ItemAtomicDisassembler;
import mekanism.common.item.ItemBalloon;
import mekanism.common.item.ItemClump;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.item.ItemCrystal;
import mekanism.common.item.ItemDictionary;
import mekanism.common.item.ItemDirtyDust;
import mekanism.common.item.ItemDust;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.item.ItemIngot;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemMekanism;
import mekanism.common.item.ItemNetworkReader;
import mekanism.common.item.ItemNugget;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.ItemShard;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.tier.AlloyTier;
import mekanism.common.tier.BaseTier;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Is some variant of the object holder thing needed here
public enum MekanismItem {
    ELECTRIC_BOW("electric_bow", new ItemElectricBow()),
    ROBIT("robit", new ItemRobit()),
    ATOMIC_DISASSEMBLER("atomic_disassembler", new ItemAtomicDisassembler()),
    ENERGY_TABLET("energy_tablet", new ItemEnergized(1000000)),
    CONFIGURATOR("configurator", new ItemConfigurator()),
    NETWORK_READER("network_reader", new ItemNetworkReader()),
    WALKIE_TALKIE("walkie_talkie", new ItemWalkieTalkie()),
    DICTIONARY("dictionary", new ItemDictionary()),
    GAS_MASK("gas_mask", new ItemGasMask()),
    SCUBA_TANK("scuba_tank", new ItemScubaTank()),
    PORTABLE_TELEPORTER("portable_teleporter", new ItemPortableTeleporter()),
    FREE_RUNNERS("free_runners", new ItemFreeRunners()),
    CONFIGURATION_CARD("configuration_card", new ItemConfigurationCard()),
    CRAFTING_FORMULA("crafting_formula", new ItemCraftingFormula()),
    SEISMIC_READER("seismic_reader", new ItemSeismicReader()),
    FLAMETHROWER("flamethrower", new ItemFlamethrower()),
    GAUGE_DROPPER("gauge_dropper", new ItemGaugeDropper()),
    TELEPORTATION_CORE("teleportation_core"),
    ENRICHED_IRON("enriched_iron"),
    ELECTROLYTIC_CORE("electrolytic_core"),
    SAWDUST("sawdust"),
    SALT("salt"),
    SUBSTRATE("substrate"),
    BIO_FUEL("bio_fuel"),

    COMPRESSED_CARBON("compressed_carbon"),
    COMPRESSED_REDSTONE("compressed_redstone"),
    COMPRESSED_DIAMOND("compressed_diamond"),
    COMPRESSED_OBSIDIAN("compressed_obsidian"),

    //Upgrade names are upgrade_type for purposes of tab complete
    SPEED_UPGRADE("upgrade_speed", new ItemUpgrade(Upgrade.SPEED)),
    ENERGY_UPGRADE("upgrade_energy", new ItemUpgrade(Upgrade.ENERGY)),
    FILTER_UPGRADE("upgrade_filter", new ItemUpgrade(Upgrade.FILTER)),
    MUFFLING_UPGRADE("upgrade_muffling", new ItemUpgrade(Upgrade.MUFFLING)),
    GAS_UPGRADE("upgrade_gas", new ItemUpgrade(Upgrade.GAS)),
    ANCHOR_UPGRADE("upgrade_anchor", new ItemUpgrade(Upgrade.ANCHOR)),

    //TODO: Split the things below here
    //Alloy names are alloy_type for purposes of tab complete
    ENRICHED_ALLOY("alloy_enriched", new ItemAlloy(AlloyTier.ENRICHED)),
    REINFORCED_ALLOY("alloy_reinforced", new ItemAlloy(AlloyTier.REINFORCED)),
    ATOMIC_ALLOY("alloy_atomic", new ItemAlloy(AlloyTier.ATOMIC)),

    BASIC_CONTROL_CIRCUIT("basic_control_circuit"),
    ADVANCED_CONTROL_CIRCUIT("advanced_control_circuit"),
    ELITE_CONTROL_CIRCUIT("elite_control_circuit"),
    ULTIMATE_CONTROL_CIRCUIT("ultimate_control_circuit"),

    JETPACK("jetpack", new ItemJetpack()),
    ARMORED_JETPACK("jetpack_armored", new ItemArmoredJetpack()),

    BLACK_BALLOON("balloon_black", new ItemBalloon(EnumColor.BLACK)),
    RED_BALLOON("balloon_red", new ItemBalloon(EnumColor.RED)),
    GREEN_BALLOON("balloon_green", new ItemBalloon(EnumColor.DARK_GREY)),
    BROWN_BALLOON("balloon_brown", new ItemBalloon(EnumColor.BROWN)),
    BLUE_BALLOON("balloon_blue", new ItemBalloon(EnumColor.DARK_BLUE)),
    PURPLE_BALLOON("balloon_purple", new ItemBalloon(EnumColor.PURPLE)),
    CYAN_BALLOON("balloon_cyan", new ItemBalloon(EnumColor.DARK_AQUA)),
    LIGHT_GREY_BALLOON("balloon_light_grey", new ItemBalloon(EnumColor.GREY)),
    GREY_BALLOON("balloon_grey", new ItemBalloon(EnumColor.DARK_GREY)),
    PINK_BALLOON("balloon_pink", new ItemBalloon(EnumColor.BRIGHT_PINK)),
    LIME_BALLOON("balloon_lime", new ItemBalloon(EnumColor.BRIGHT_GREEN)),
    YELLOW_BALLOON("balloon_yellow", new ItemBalloon(EnumColor.YELLOW)),
    LIGHT_BLUE_BALLOON("balloon_light_blue", new ItemBalloon(EnumColor.INDIGO)),
    MAGENTA_BALLOON("balloon_magenta", new ItemBalloon(EnumColor.PINK)),
    ORANGE_BALLOON("balloon_orange", new ItemBalloon(EnumColor.ORANGE)),
    WHITE_BALLOON("balloon_white", new ItemBalloon(EnumColor.WHITE)),

    HDPE_PELLET("hdpe_pellet"),
    HDPE_ROD("hdpe_rod"),
    HDPE_SHEET("hdpe_sheet"),
    HDPE_STICK("hdpe_stick"),

    BASIC_TIER_INSTALLER("basic_tier_installer", new ItemTierInstaller(BaseTier.BASIC)),
    ADVANCED_TIER_INSTALLER("advanced_tier_installer", new ItemTierInstaller(BaseTier.ADVANCED)),
    ELITE_TIER_INSTALLER("elite_tier_installer", new ItemTierInstaller(BaseTier.ELITE)),
    ULTIMATE_TIER_INSTALLER("ultimate_tier_installer", new ItemTierInstaller(BaseTier.ULTIMATE)),

    //TODO: Have below stuff auto register oredict entry on constructor creation
    IRON_CRYSTAL("crystal_iron", new ItemCrystal()),
    GOLD_CRYSTAL("crystal_gold", new ItemCrystal()),
    OSMIUM_CRYSTAL("crystal_osmium", new ItemCrystal()),
    COPPER_CRYSTAL("crystal_copper", new ItemCrystal()),
    TIN_CRYSTAL("crystal_tin", new ItemCrystal()),
    SILVER_CRYSTAL("crystal_silver", new ItemCrystal()),
    LEAD_CRYSTAL("crystal_lead", new ItemCrystal()),

    IRON_SHARD("shard_iron", new ItemShard()),
    GOLD_SHARD("shard_gold", new ItemShard()),
    OSMIUM_SHARD("shard_osmium", new ItemShard()),
    COPPER_SHARD("shard_copper", new ItemShard()),
    TIN_SHARD("shard_tin", new ItemShard()),
    SILVER_SHARD("shard_silver", new ItemShard()),
    LEAD_SHARD("shard_lead", new ItemShard()),

    IRON_CLUMP("clump_iron", new ItemClump()),
    GOLD_CLUMP("clump_gold", new ItemClump()),
    OSMIUM_CLUMP("clump_osmium", new ItemClump()),
    COPPER_CLUMP("clump_copper", new ItemClump()),
    TIN_CLUMP("clump_tin", new ItemClump()),
    SILVER_CLUMP("clump_silver", new ItemClump()),
    LEAD_CLUMP("clump_lead", new ItemClump()),

    DIRTY_IRON_DUST("dirty_dust_iron", new ItemDirtyDust()),
    DIRT_GOLD_DUST("dirty_dust_gold", new ItemDirtyDust()),
    DIRTY_OSMIUM_DUST("dirty_dust_osmium", new ItemDirtyDust()),
    DIRT_COPPER_DUST("dirty_dust_copper", new ItemDirtyDust()),
    DIRTY_TIN_DUST("dirty_dust_tin", new ItemDirtyDust()),
    DIRTY_SILVER_DUST("dirty_dust_silver", new ItemDirtyDust()),
    DIRTY_LEAD_DUST("dirty_dust_lead", new ItemDirtyDust()),

    IRON_DUST("dust_iron", new ItemDust()),
    GOLD_DUST("dust_gold", new ItemDust()),
    OSMIUM_DUST("dust_osmium", new ItemDust()),
    COPPER_DUST("dust_copper", new ItemDust()),
    TIN_DUST("dust_tin", new ItemDust()),
    SILVER_DUST("dust_silver", new ItemDust()),
    LEAD_DUST("dust_lead", new ItemDust()),

    DIAMOND_DUST("dust_diamond", new ItemDust()),
    STEEL_DUST("dust_steel", new ItemDust()),
    SULFUR_DUST("dust_sulfur", new ItemDust()),
    LITHIUM_DUST("dust_lithium", new ItemDust()),
    REFINED_OBSIDIAN_DUST("dust_refined_obsidian", new ItemDust()),
    OBSIDIAN_DUST("dust_obsidian", new ItemDust()),

    REFINED_OBSIDIAN_INGOT("ingot_refined_obsidian", new ItemIngot()),
    OSMIUM_INGOT("ingot_osmium", new ItemIngot()),
    BRONZE_INGOT("ingot_bronze", new ItemIngot()),
    GLOWSTONE_INGOT("ingot_glowstone", new ItemIngot()),
    STEEL_INGOT("ingot_steel", new ItemIngot()),
    COPPER_INGOT("ingot_copper", new ItemIngot()),
    TIN_INGOT("ingot_tin", new ItemIngot()),

    REFINED_OBSIDIAN_NUGGET("nugget_refined_obsidian", new ItemNugget()),
    OSMIUM_NUGGET("nugget_osmium", new ItemNugget()),
    BRONZE_NUGGET("nugget_bronze", new ItemNugget()),
    GLOWSTONE_NUGGET("nugget_glowstone", new ItemNugget()),
    STEEL_NUGGET("nugget_steel", new ItemNugget()),
    COPPER_NUGGET("nugget_copper", new ItemNugget()),
    TIN_NUGGET("nugget_tin", new ItemNugget());

    private final String name;
    private final Item item;

    MekanismItem(String name) {
        this(name, new ItemMekanism());
    }

    MekanismItem(String name, Item item) {
        this.item = item;
        this.name = name;
        //TODO: Pass this information to the item itself and have it initialize it in the constructor
        init();
    }

    private void init() {
        item.setTranslationKey(getTranslationKey());
        item.setRegistryName(new ResourceLocation(Mekanism.MODID, name));
    }

    public String getTranslationKey() {
        return "item.mekanism." + name;
    }

    public String getName() {
        return name;
    }

    public Item getItem() {
        return item;
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (MekanismItem mekanismItem : values()) {
            registry.register(mekanismItem.getItem());
        }
    }
}