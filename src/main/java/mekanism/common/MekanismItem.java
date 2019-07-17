package mekanism.common;

import mekanism.api.EnumColor;
import mekanism.common.item.ItemAlloy;
import mekanism.common.item.ItemArmoredJetpack;
import mekanism.common.item.ItemAtomicDisassembler;
import mekanism.common.item.ItemBalloon;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.item.ItemDictionary;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemMekanism;
import mekanism.common.item.ItemNetworkReader;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemResource;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.resource.ResourceType;
import mekanism.common.tier.AlloyTier;
import mekanism.common.tier.BaseTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
    IRON_CRYSTAL("crystal_iron", new ItemResource(ResourceType.CRYSTAL)),
    GOLD_CRYSTAL("crystal_gold", new ItemResource(ResourceType.CRYSTAL)),
    OSMIUM_CRYSTAL("crystal_osmium", new ItemResource(ResourceType.CRYSTAL)),
    COPPER_CRYSTAL("crystal_copper", new ItemResource(ResourceType.CRYSTAL)),
    TIN_CRYSTAL("crystal_tin", new ItemResource(ResourceType.CRYSTAL)),
    SILVER_CRYSTAL("crystal_silver", new ItemResource(ResourceType.CRYSTAL)),
    LEAD_CRYSTAL("crystal_lead", new ItemResource(ResourceType.CRYSTAL)),

    IRON_SHARD("shard_iron", new ItemResource(ResourceType.SHARD)),
    GOLD_SHARD("shard_gold", new ItemResource(ResourceType.SHARD)),
    OSMIUM_SHARD("shard_osmium", new ItemResource(ResourceType.SHARD)),
    COPPER_SHARD("shard_copper", new ItemResource(ResourceType.SHARD)),
    TIN_SHARD("shard_tin", new ItemResource(ResourceType.SHARD)),
    SILVER_SHARD("shard_silver", new ItemResource(ResourceType.SHARD)),
    LEAD_SHARD("shard_lead", new ItemResource(ResourceType.SHARD)),

    IRON_CLUMP("clump_iron", new ItemResource(ResourceType.CLUMP)),
    GOLD_CLUMP("clump_gold", new ItemResource(ResourceType.CLUMP)),
    OSMIUM_CLUMP("clump_osmium", new ItemResource(ResourceType.CLUMP)),
    COPPER_CLUMP("clump_copper", new ItemResource(ResourceType.CLUMP)),
    TIN_CLUMP("clump_tin", new ItemResource(ResourceType.CLUMP)),
    SILVER_CLUMP("clump_silver", new ItemResource(ResourceType.CLUMP)),
    LEAD_CLUMP("clump_lead", new ItemResource(ResourceType.CLUMP)),

    DIRTY_IRON_DUST("dirty_dust_iron", new ItemResource(ResourceType.DIRT_DUST)),
    DIRT_GOLD_DUST("dirty_dust_gold", new ItemResource(ResourceType.DIRT_DUST)),
    DIRTY_OSMIUM_DUST("dirty_dust_osmium", new ItemResource(ResourceType.DIRT_DUST)),
    DIRT_COPPER_DUST("dirty_dust_copper", new ItemResource(ResourceType.DIRT_DUST)),
    DIRTY_TIN_DUST("dirty_dust_tin", new ItemResource(ResourceType.DIRT_DUST)),
    DIRTY_SILVER_DUST("dirty_dust_silver", new ItemResource(ResourceType.DIRT_DUST)),
    DIRTY_LEAD_DUST("dirty_dust_lead", new ItemResource(ResourceType.DIRT_DUST)),

    IRON_DUST("dust_iron", new ItemResource(ResourceType.DUST)),
    GOLD_DUST("dust_gold", new ItemResource(ResourceType.DUST)),
    OSMIUM_DUST("dust_osmium", new ItemResource(ResourceType.DUST)),
    COPPER_DUST("dust_copper", new ItemResource(ResourceType.DUST)),
    TIN_DUST("dust_tin", new ItemResource(ResourceType.DUST)),
    SILVER_DUST("dust_silver", new ItemResource(ResourceType.DUST)),
    LEAD_DUST("dust_lead", new ItemResource(ResourceType.DUST)),

    DIAMOND_DUST("dust_diamond", new ItemResource(ResourceType.DUST)),
    STEEL_DUST("dust_steel", new ItemResource(ResourceType.DUST)),
    SULFUR_DUST("dust_sulfur", new ItemResource(ResourceType.DUST)),
    LITHIUM_DUST("dust_lithium", new ItemResource(ResourceType.DUST)),
    REFINED_OBSIDIAN_DUST("dust_refined_obsidian", new ItemResource(ResourceType.DUST)),
    OBSIDIAN_DUST("dust_obsidian", new ItemResource(ResourceType.DUST)),

    REFINED_OBSIDIAN_INGOT("ingot_refined_obsidian", new ItemResource(ResourceType.INGOT)),
    OSMIUM_INGOT("ingot_osmium", new ItemResource(ResourceType.INGOT)),
    BRONZE_INGOT("ingot_bronze", new ItemResource(ResourceType.INGOT)),
    GLOWSTONE_INGOT("ingot_glowstone", new ItemResource(ResourceType.INGOT)),
    STEEL_INGOT("ingot_steel", new ItemResource(ResourceType.INGOT)),
    COPPER_INGOT("ingot_copper", new ItemResource(ResourceType.INGOT)),
    TIN_INGOT("ingot_tin", new ItemResource(ResourceType.INGOT)),

    REFINED_OBSIDIAN_NUGGET("nugget_refined_obsidian", new ItemResource(ResourceType.NUGGET)),
    OSMIUM_NUGGET("nugget_osmium", new ItemResource(ResourceType.NUGGET)),
    BRONZE_NUGGET("nugget_bronze", new ItemResource(ResourceType.NUGGET)),
    GLOWSTONE_NUGGET("nugget_glowstone", new ItemResource(ResourceType.NUGGET)),
    STEEL_NUGGET("nugget_steel", new ItemResource(ResourceType.NUGGET)),
    COPPER_NUGGET("nugget_copper", new ItemResource(ResourceType.NUGGET)),
    TIN_NUGGET("nugget_tin", new ItemResource(ResourceType.NUGGET));

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

    public ItemStack getItemStack() {
        return getItemStack(1);
    }

    public ItemStack getItemStack(int size) {
        return new ItemStack(getItem(), size);
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (MekanismItem mekanismItem : values()) {
            registry.register(mekanismItem.getItem());
        }
    }
}