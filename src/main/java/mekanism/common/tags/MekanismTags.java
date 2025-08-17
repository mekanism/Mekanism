package mekanism.common.tags;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.resource.IResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class MekanismTags {

    private MekanismTags() {
    }

    public static class Items {

        private Items() {
        }

        public static final Table<ResourceType, PrimaryResource, TagKey<Item>> PROCESSED_RESOURCES = HashBasedTable.create();
        public static final Map<IResource, TagKey<Item>> PROCESSED_RESOURCE_BLOCKS = new HashMap<>();
        public static final Map<OreType, TagKey<Item>> ORES = new EnumMap<>(OreType.class);

        static {
            for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
                for (ResourceType type : EnumUtils.RESOURCE_TYPES) {
                    if (type.usedByPrimary(resource)) {
                        if (type.isVanilla() || type == ResourceType.DUST) {
                            PROCESSED_RESOURCES.put(type, resource, commonTag(type.getBaseTagPath() + "/" + resource.getRegistrySuffix()));
                        } else {
                            PROCESSED_RESOURCES.put(type, resource, commonTag(type.getBaseTagPath() + "/" + resource.getRegistrySuffix()));
                        }
                    }
                }
                if (!resource.isVanilla()) {
                    PROCESSED_RESOURCE_BLOCKS.put(resource, commonTag("storage_blocks/" + resource.getRegistrySuffix()));
                    BlockResourceInfo rawResource = resource.getRawResourceBlockInfo();
                    if (rawResource != null) {
                        PROCESSED_RESOURCE_BLOCKS.put(rawResource, commonTag("storage_blocks/" + rawResource.getRegistrySuffix()));
                    }
                }
            }
            for (OreType ore : EnumUtils.ORE_TYPES) {
                ORES.put(ore, commonTag("ores/" + ore.getResource().getRegistrySuffix()));
            }
        }

        public static final TagKey<Item> CONFIGURATORS = tag("configurators");
        public static final TagKey<Item> PERSONAL_STORAGE = tag("personal_storage");

        public static final TagKey<Item> RODS_PLASTIC = commonTag("rods/plastic");

        public static final TagKey<Item> FUELS = commonTag("fuels");
        public static final TagKey<Item> FUELS_BIO = commonTag("fuels/bio");
        public static final TagKey<Item> FUELS_BLOCK_BIO = commonTag("fuels/block/bio");

        public static final TagKey<Item> PELLETS_ANTIMATTER = commonTag("pellets/antimatter");
        public static final TagKey<Item> PELLETS_PLUTONIUM = commonTag("pellets/plutonium");
        public static final TagKey<Item> PELLETS_POLONIUM = commonTag("pellets/polonium");

        public static final TagKey<Item> DUSTS_BRONZE = commonTag("dusts/bronze");
        public static final TagKey<Item> DUSTS_CHARCOAL = commonTag("dusts/charcoal");
        public static final TagKey<Item> DUSTS_COAL = commonTag("dusts/coal");
        public static final TagKey<Item> DUSTS_DIAMOND = commonTag("dusts/diamond");
        public static final TagKey<Item> DUSTS_EMERALD = commonTag("dusts/emerald");
        public static final TagKey<Item> DUSTS_NETHERITE = commonTag("dusts/netherite");
        public static final TagKey<Item> DUSTS_LAPIS = commonTag("dusts/lapis");
        public static final TagKey<Item> DUSTS_LITHIUM = commonTag("dusts/lithium");
        public static final TagKey<Item> DUSTS_OBSIDIAN = commonTag("dusts/obsidian");
        public static final TagKey<Item> DUSTS_QUARTZ = commonTag("dusts/quartz");
        public static final TagKey<Item> DUSTS_REFINED_OBSIDIAN = commonTag("dusts/refined_obsidian");
        public static final TagKey<Item> DUSTS_SALT = commonTag("dusts/salt");
        public static final TagKey<Item> DUSTS_STEEL = commonTag("dusts/steel");
        public static final TagKey<Item> DUSTS_SULFUR = commonTag("dusts/sulfur");
        public static final TagKey<Item> DUSTS_WOOD = commonTag("dusts/wood");
        public static final TagKey<Item> DUSTS_FLUORITE = commonTag("dusts/fluorite");

        public static final TagKey<Item> NUGGETS_BRONZE = commonTag("nuggets/bronze");
        public static final TagKey<Item> NUGGETS_REFINED_GLOWSTONE = commonTag("nuggets/refined_glowstone");
        public static final TagKey<Item> NUGGETS_REFINED_OBSIDIAN = commonTag("nuggets/refined_obsidian");
        public static final TagKey<Item> NUGGETS_STEEL = commonTag("nuggets/steel");

        public static final TagKey<Item> INGOTS_BRONZE = commonTag("ingots/bronze");
        public static final TagKey<Item> INGOTS_REFINED_GLOWSTONE = commonTag("ingots/refined_glowstone");
        public static final TagKey<Item> INGOTS_REFINED_OBSIDIAN = commonTag("ingots/refined_obsidian");
        public static final TagKey<Item> INGOTS_STEEL = commonTag("ingots/steel");

        public static final TagKey<Item> STORAGE_BLOCKS_BRONZE = commonTag("storage_blocks/bronze");
        public static final TagKey<Item> STORAGE_BLOCKS_CHARCOAL = commonTag("storage_blocks/charcoal");
        public static final TagKey<Item> STORAGE_BLOCKS_REFINED_GLOWSTONE = commonTag("storage_blocks/refined_glowstone");
        public static final TagKey<Item> STORAGE_BLOCKS_REFINED_OBSIDIAN = commonTag("storage_blocks/refined_obsidian");
        public static final TagKey<Item> STORAGE_BLOCKS_STEEL = commonTag("storage_blocks/steel");
        public static final TagKey<Item> STORAGE_BLOCKS_FLUORITE = commonTag("storage_blocks/fluorite");

        public static final TagKey<Item> CIRCUITS = commonTag("circuits");
        public static final TagKey<Item> CIRCUITS_BASIC = commonTag("circuits/basic");
        public static final TagKey<Item> CIRCUITS_ADVANCED = commonTag("circuits/advanced");
        public static final TagKey<Item> CIRCUITS_ELITE = commonTag("circuits/elite");
        public static final TagKey<Item> CIRCUITS_ULTIMATE = commonTag("circuits/ultimate");

        public static final TagKey<Item> ALLOYS = tag("alloys");
        public static final TagKey<Item> ALLOYS_BASIC = tag("alloys/basic");
        public static final TagKey<Item> ALLOYS_INFUSED = tag("alloys/infused");
        public static final TagKey<Item> ALLOYS_REINFORCED = tag("alloys/reinforced");
        public static final TagKey<Item> ALLOYS_ATOMIC = tag("alloys/atomic");
        //Forge alloy tags
        public static final TagKey<Item> COMMON_ALLOYS = commonTag("alloys");
        public static final TagKey<Item> ALLOYS_ADVANCED = commonTag("alloys/advanced");
        public static final TagKey<Item> ALLOYS_ELITE = commonTag("alloys/elite");
        public static final TagKey<Item> ALLOYS_ULTIMATE = commonTag("alloys/ultimate");

        public static final TagKey<Item> ENRICHED = tag("enriched");
        public static final TagKey<Item> ENRICHED_CARBON = tag("enriched/carbon");
        public static final TagKey<Item> ENRICHED_DIAMOND = tag("enriched/diamond");
        public static final TagKey<Item> ENRICHED_OBSIDIAN = tag("enriched/obsidian");
        public static final TagKey<Item> ENRICHED_REDSTONE = tag("enriched/redstone");
        public static final TagKey<Item> ENRICHED_GOLD = tag("enriched/gold");
        public static final TagKey<Item> ENRICHED_TIN = tag("enriched/tin");

        public static final TagKey<Item> DIRTY_DUSTS = tag("dirty_dusts");
        public static final TagKey<Item> CLUMPS = tag("clumps");
        public static final TagKey<Item> SHARDS = tag("shards");
        public static final TagKey<Item> CRYSTALS = tag("crystals");

        public static final TagKey<Item> GEMS_FLUORITE = commonTag("gems/fluorite");

        public static final TagKey<Item> MEKASUIT_HUD_RENDERER = tag("mekasuit_hud_renderer");
        public static final TagKey<Item> STONE_CRAFTING_MATERIALS = tag("stone_crafting_materials");
        public static final TagKey<Item> MUFFLING_CENTER = tag("muffling_center");

        public static final TagKey<Item> COLORABLE_WOOL = tag("colorable/wool");
        public static final TagKey<Item> COLORABLE_CARPETS = tag("colorable/carpets");
        public static final TagKey<Item> COLORABLE_BEDS = tag("colorable/beds");
        public static final TagKey<Item> COLORABLE_GLASS = tag("colorable/glass");
        public static final TagKey<Item> COLORABLE_GLASS_PANES = tag("colorable/glass_panes");
        public static final TagKey<Item> COLORABLE_TERRACOTTA = tag("colorable/terracotta");
        public static final TagKey<Item> COLORABLE_CANDLE = tag("colorable/candle");
        public static final TagKey<Item> COLORABLE_CONCRETE = tag("colorable/concrete");
        public static final TagKey<Item> COLORABLE_CONCRETE_POWDER = tag("colorable/concrete_powder");
        public static final TagKey<Item> COLORABLE_BANNERS = tag("colorable/banners");

        private static TagKey<Item> commonTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(Mekanism.rl(name));
        }
    }

    public static class Blocks {

        private Blocks() {
        }

        public static final Map<IResource, TagKey<Block>> RESOURCE_STORAGE_BLOCKS = new HashMap<>();
        public static final Map<OreType, TagKey<Block>> ORES = new EnumMap<>(OreType.class);

        static {
            for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
                if (!resource.isVanilla()) {
                    RESOURCE_STORAGE_BLOCKS.put(resource, commonTag("storage_blocks/" + resource.getRegistrySuffix()));
                    BlockResourceInfo rawResource = resource.getRawResourceBlockInfo();
                    if (rawResource != null) {
                        RESOURCE_STORAGE_BLOCKS.put(rawResource, commonTag("storage_blocks/" + rawResource.getRegistrySuffix()));
                    }
                }
            }
            for (OreType ore : EnumUtils.ORE_TYPES) {
                ORES.put(ore, commonTag("ores/" + ore.getResource().getRegistrySuffix()));
            }
        }

        public static final TagKey<Block> CARDBOARD_BLACKLIST = tag("cardboard_blacklist");
        public static final TagKey<Block> MINER_BLACKLIST = tag("miner_blacklist");
        public static final TagKey<Block> ATOMIC_DISASSEMBLER_ORE = tag("atomic_disassembler_ore");
        public static final TagKey<Block> INCORRECT_FOR_DISASSEMBLER = tag("incorrect_for_disassembler");
        public static final TagKey<Block> INCORRECT_FOR_MEKA_TOOL = tag("incorrect_for_meka_tool");
        /**
         * For use in the farming module to target blocks that should be effectively ignored when checking if the block below should be targeted.
         */
        public static final TagKey<Block> FARMING_OVERRIDE = tag("farming_override");

        public static final TagKey<Block> CHESTS_ELECTRIC = commonTag("chests/electric");
        public static final TagKey<Block> CHESTS_PERSONAL = commonTag("chests/personal");
        public static final TagKey<Block> BARRELS_PERSONAL = commonTag("barrels/personal");
        public static final TagKey<Block> PERSONAL_STORAGE = tag("personal_storage");

        public static final TagKey<Block> STORAGE_BLOCKS_BRONZE = commonTag("storage_blocks/bronze");
        public static final TagKey<Block> STORAGE_BLOCKS_CHARCOAL = commonTag("storage_blocks/charcoal");
        public static final TagKey<Block> STORAGE_BLOCKS_REFINED_GLOWSTONE = commonTag("storage_blocks/refined_glowstone");
        public static final TagKey<Block> STORAGE_BLOCKS_REFINED_OBSIDIAN = commonTag("storage_blocks/refined_obsidian");
        public static final TagKey<Block> STORAGE_BLOCKS_STEEL = commonTag("storage_blocks/steel");
        public static final TagKey<Block> STORAGE_BLOCKS_FLUORITE = commonTag("storage_blocks/fluorite");

        private static TagKey<Block> commonTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(Mekanism.rl(name));
        }
    }

    public static class Biomes {

        private Biomes() {
        }

        public static final TagKey<Biome> SPAWN_ORES = tag("spawn_ores");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, Mekanism.rl(name));
        }
    }

    public static class Entities {

        private Entities() {
        }

        public static final TagKey<EntityType<?>> CREEPERS = commonTag("creepers");
        public static final TagKey<EntityType<?>> VALID_SPS_EXPERIMENT = tag("valid_sps_experiment");

        private static TagKey<EntityType<?>> commonTag(String name) {
            return tag(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<EntityType<?>> tag(String name) {
            return tag(Mekanism.rl(name));
        }

        private static TagKey<EntityType<?>> tag(ResourceLocation tag) {
            return TagKey.create(Registries.ENTITY_TYPE, tag);
        }
    }

    public static class Fluids {

        private Fluids() {
        }

        public static final TagKey<Fluid> BRINE = commonTag("brine");
        public static final TagKey<Fluid> CHLORINE = commonTag("chlorine");
        public static final TagKey<Fluid> ETHENE = commonTag("ethene");
        public static final TagKey<Fluid> HEAVY_WATER = commonTag("heavy_water");
        public static final TagKey<Fluid> HYDROGEN = commonTag("hydrogen");
        public static final TagKey<Fluid> HYDROGEN_CHLORIDE = commonTag("hydrogen_chloride");
        public static final TagKey<Fluid> URANIUM_OXIDE = commonTag("uranium_oxide");
        public static final TagKey<Fluid> URANIUM_HEXAFLUORIDE = commonTag("uranium_hexafluoride");
        public static final TagKey<Fluid> LITHIUM = commonTag("lithium");
        public static final TagKey<Fluid> OXYGEN = commonTag("oxygen");
        public static final TagKey<Fluid> SODIUM = commonTag("sodium");
        public static final TagKey<Fluid> SUPERHEATED_SODIUM = commonTag("superheated_sodium");
        public static final TagKey<Fluid> STEAM = commonTag("steam");
        public static final TagKey<Fluid> SULFUR_DIOXIDE = commonTag("sulfur_dioxide");
        public static final TagKey<Fluid> SULFUR_TRIOXIDE = commonTag("sulfur_trioxide");
        public static final TagKey<Fluid> SULFURIC_ACID = commonTag("sulfuric_acid");
        public static final TagKey<Fluid> HYDROFLUORIC_ACID = commonTag("hydrofluoric_acid");
        public static final TagKey<Fluid> NUTRITIONAL_PASTE = commonTag("nutritional_paste");

        private static TagKey<Fluid> commonTag(String name) {
            return FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static class Chemicals {

        private Chemicals() {
        }

        public static final TagKey<Chemical> WATER_VAPOR = tag("water_vapor");

        private static TagKey<Chemical> tag(String name) {
            return TagKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, Mekanism.rl(name));
        }
    }

    public static class DataComponents {

        private DataComponents() {
        }

        public static final TagKey<DataComponentType<?>> CLEARABLE_CONFIG = tag("config/clearable");

        private static TagKey<DataComponentType<?>> tag(String name) {
            return TagKey.create(Registries.DATA_COMPONENT_TYPE, Mekanism.rl(name));
        }
    }
}