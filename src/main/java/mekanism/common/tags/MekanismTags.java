package mekanism.common.tags;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.Mekanism;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.resource.IResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.util.EnumUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public class MekanismTags {

    /**
     * Call to force make sure this is all initialized
     */
    public static void init() {
        Items.init();
        Blocks.init();
        Entities.init();
        Fluids.init();
        Gases.init();
        InfuseTypes.init();
        Slurries.init();
        TileEntityTypes.init();
    }

    private MekanismTags() {
    }

    public static class Items {

        private static void init() {
        }

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
                            PROCESSED_RESOURCES.put(type, resource, forgeTag(type.getBaseTagPath() + "/" + resource.getRegistrySuffix()));
                        } else {
                            PROCESSED_RESOURCES.put(type, resource, tag(type.getBaseTagPath() + "/" + resource.getRegistrySuffix()));
                        }
                    }
                }
                if (!resource.isVanilla()) {
                    PROCESSED_RESOURCE_BLOCKS.put(resource, forgeTag("storage_blocks/" + resource.getRegistrySuffix()));
                    BlockResourceInfo rawResource = resource.getRawResourceBlockInfo();
                    if (rawResource != null) {
                        PROCESSED_RESOURCE_BLOCKS.put(rawResource, forgeTag("storage_blocks/" + rawResource.getRegistrySuffix()));
                    }
                }
            }
            for (OreType ore : EnumUtils.ORE_TYPES) {
                ORES.put(ore, forgeTag("ores/" + ore.getResource().getRegistrySuffix()));
            }
        }

        public static final TagKey<Item> CONFIGURATORS = tag("configurators");
        public static final TagKey<Item> WRENCHES = forgeTag("wrenches");
        public static final TagKey<Item> TOOLS = forgeTag("tools");
        public static final TagKey<Item> TOOLS_WRENCH = forgeTag("tools/wrench");
        public static final TagKey<Item> PERSONAL_STORAGE = tag("personal_storage");

        public static final TagKey<Item> BATTERIES = forgeTag("batteries");

        public static final TagKey<Item> RODS_PLASTIC = forgeTag("rods/plastic");

        public static final TagKey<Item> FUELS = forgeTag("fuels");
        public static final TagKey<Item> FUELS_BIO = forgeTag("fuels/bio");

        public static final TagKey<Item> SALT = forgeTag("salt");
        public static final TagKey<Item> SAWDUST = forgeTag("sawdust");
        public static final TagKey<Item> YELLOW_CAKE_URANIUM = forgeTag("yellow_cake_uranium");

        public static final TagKey<Item> PELLETS_ANTIMATTER = forgeTag("pellets/antimatter");
        public static final TagKey<Item> PELLETS_PLUTONIUM = forgeTag("pellets/plutonium");
        public static final TagKey<Item> PELLETS_POLONIUM = forgeTag("pellets/polonium");

        public static final TagKey<Item> DUSTS_BRONZE = forgeTag("dusts/bronze");
        public static final TagKey<Item> DUSTS_CHARCOAL = forgeTag("dusts/charcoal");
        public static final TagKey<Item> DUSTS_COAL = forgeTag("dusts/coal");
        public static final TagKey<Item> DUSTS_DIAMOND = forgeTag("dusts/diamond");
        public static final TagKey<Item> DUSTS_EMERALD = forgeTag("dusts/emerald");
        public static final TagKey<Item> DUSTS_NETHERITE = forgeTag("dusts/netherite");
        public static final TagKey<Item> DUSTS_LAPIS = forgeTag("dusts/lapis");
        public static final TagKey<Item> DUSTS_LITHIUM = forgeTag("dusts/lithium");
        public static final TagKey<Item> DUSTS_OBSIDIAN = forgeTag("dusts/obsidian");
        public static final TagKey<Item> DUSTS_QUARTZ = forgeTag("dusts/quartz");
        public static final TagKey<Item> DUSTS_REFINED_OBSIDIAN = forgeTag("dusts/refined_obsidian");
        public static final TagKey<Item> DUSTS_SALT = forgeTag("dusts/salt");
        public static final TagKey<Item> DUSTS_STEEL = forgeTag("dusts/steel");
        public static final TagKey<Item> DUSTS_SULFUR = forgeTag("dusts/sulfur");
        public static final TagKey<Item> DUSTS_WOOD = forgeTag("dusts/wood");
        public static final TagKey<Item> DUSTS_FLUORITE = forgeTag("dusts/fluorite");

        public static final TagKey<Item> NUGGETS_BRONZE = forgeTag("nuggets/bronze");
        public static final TagKey<Item> NUGGETS_REFINED_GLOWSTONE = forgeTag("nuggets/refined_glowstone");
        public static final TagKey<Item> NUGGETS_REFINED_OBSIDIAN = forgeTag("nuggets/refined_obsidian");
        public static final TagKey<Item> NUGGETS_STEEL = forgeTag("nuggets/steel");

        public static final TagKey<Item> INGOTS_BRONZE = forgeTag("ingots/bronze");
        public static final TagKey<Item> INGOTS_REFINED_GLOWSTONE = forgeTag("ingots/refined_glowstone");
        public static final TagKey<Item> INGOTS_REFINED_OBSIDIAN = forgeTag("ingots/refined_obsidian");
        public static final TagKey<Item> INGOTS_STEEL = forgeTag("ingots/steel");

        public static final TagKey<Item> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");
        public static final TagKey<Item> STORAGE_BLOCKS_CHARCOAL = forgeTag("storage_blocks/charcoal");
        public static final TagKey<Item> STORAGE_BLOCKS_REFINED_GLOWSTONE = forgeTag("storage_blocks/refined_glowstone");
        public static final TagKey<Item> STORAGE_BLOCKS_REFINED_OBSIDIAN = forgeTag("storage_blocks/refined_obsidian");
        public static final TagKey<Item> STORAGE_BLOCKS_STEEL = forgeTag("storage_blocks/steel");
        public static final TagKey<Item> STORAGE_BLOCKS_FLUORITE = forgeTag("storage_blocks/fluorite");

        public static final TagKey<Item> CIRCUITS = forgeTag("circuits");
        public static final TagKey<Item> CIRCUITS_BASIC = forgeTag("circuits/basic");
        public static final TagKey<Item> CIRCUITS_ADVANCED = forgeTag("circuits/advanced");
        public static final TagKey<Item> CIRCUITS_ELITE = forgeTag("circuits/elite");
        public static final TagKey<Item> CIRCUITS_ULTIMATE = forgeTag("circuits/ultimate");

        public static final TagKey<Item> ALLOYS = tag("alloys");
        public static final TagKey<Item> ALLOYS_BASIC = tag("alloys/basic");
        public static final TagKey<Item> ALLOYS_INFUSED = tag("alloys/infused");
        public static final TagKey<Item> ALLOYS_REINFORCED = tag("alloys/reinforced");
        public static final TagKey<Item> ALLOYS_ATOMIC = tag("alloys/atomic");
        //Forge alloy tags
        public static final TagKey<Item> FORGE_ALLOYS = forgeTag("alloys");
        public static final TagKey<Item> ALLOYS_ADVANCED = forgeTag("alloys/advanced");
        public static final TagKey<Item> ALLOYS_ELITE = forgeTag("alloys/elite");
        public static final TagKey<Item> ALLOYS_ULTIMATE = forgeTag("alloys/ultimate");

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

        public static final TagKey<Item> GEMS_FLUORITE = forgeTag("gems/fluorite");

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

        private static TagKey<Item> forgeTag(String name) {
            return ItemTags.create(new ResourceLocation("forge", name));
        }

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(Mekanism.rl(name));
        }
    }

    public static class Blocks {

        private static void init() {
        }

        private Blocks() {
        }

        public static final Map<IResource, TagKey<Block>> RESOURCE_STORAGE_BLOCKS = new HashMap<>();
        public static final Map<OreType, TagKey<Block>> ORES = new EnumMap<>(OreType.class);

        static {
            for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
                if (!resource.isVanilla()) {
                    RESOURCE_STORAGE_BLOCKS.put(resource, forgeTag("storage_blocks/" + resource.getRegistrySuffix()));
                    BlockResourceInfo rawResource = resource.getRawResourceBlockInfo();
                    if (rawResource != null) {
                        RESOURCE_STORAGE_BLOCKS.put(rawResource, forgeTag("storage_blocks/" + rawResource.getRegistrySuffix()));
                    }
                }
            }
            for (OreType ore : EnumUtils.ORE_TYPES) {
                ORES.put(ore, forgeTag("ores/" + ore.getResource().getRegistrySuffix()));
            }
        }

        public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = forgeTag("relocation_not_supported");
        public static final TagKey<Block> CARDBOARD_BLACKLIST = tag("cardboard_blacklist");
        public static final TagKey<Block> MINER_BLACKLIST = tag("miner_blacklist");
        public static final LazyTagLookup<Block> MINER_BLACKLIST_LOOKUP = LazyTagLookup.create(ForgeRegistries.BLOCKS, MINER_BLACKLIST);
        public static final TagKey<Block> ATOMIC_DISASSEMBLER_ORE = tag("atomic_disassembler_ore");

        public static final TagKey<Block> CHESTS_ELECTRIC = forgeTag("chests/electric");
        public static final TagKey<Block> CHESTS_PERSONAL = forgeTag("chests/personal");
        public static final TagKey<Block> BARRELS_PERSONAL = forgeTag("barrels/personal");
        public static final TagKey<Block> PERSONAL_STORAGE = tag("personal_storage");

        public static final TagKey<Block> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");
        public static final TagKey<Block> STORAGE_BLOCKS_CHARCOAL = forgeTag("storage_blocks/charcoal");
        public static final TagKey<Block> STORAGE_BLOCKS_REFINED_GLOWSTONE = forgeTag("storage_blocks/refined_glowstone");
        public static final TagKey<Block> STORAGE_BLOCKS_REFINED_OBSIDIAN = forgeTag("storage_blocks/refined_obsidian");
        public static final TagKey<Block> STORAGE_BLOCKS_STEEL = forgeTag("storage_blocks/steel");
        public static final TagKey<Block> STORAGE_BLOCKS_FLUORITE = forgeTag("storage_blocks/fluorite");

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(Mekanism.rl(name));
        }
    }

    public static class Entities {

        private static void init() {
        }

        private Entities() {
        }

        public static final TagKey<EntityType<?>> HURTABLE_VEHICLES = tag("hurtable_vehicles");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagUtils.createKey(ForgeRegistries.ENTITIES, Mekanism.rl(name));
        }
    }

    public static class Fluids {

        private static void init() {
        }

        private Fluids() {
        }

        public static final TagKey<Fluid> BRINE = forgeTag("brine");
        public static final TagKey<Fluid> CHLORINE = forgeTag("chlorine");
        public static final TagKey<Fluid> ETHENE = forgeTag("ethene");
        public static final TagKey<Fluid> HEAVY_WATER = forgeTag("heavy_water");
        public static final TagKey<Fluid> HYDROGEN = forgeTag("hydrogen");
        public static final TagKey<Fluid> HYDROGEN_CHLORIDE = forgeTag("hydrogen_chloride");
        public static final TagKey<Fluid> URANIUM_OXIDE = forgeTag("uranium_oxide");
        public static final TagKey<Fluid> URANIUM_HEXAFLUORIDE = forgeTag("uranium_hexafluoride");
        public static final TagKey<Fluid> LITHIUM = forgeTag("lithium");
        public static final TagKey<Fluid> OXYGEN = forgeTag("oxygen");
        public static final TagKey<Fluid> SODIUM = forgeTag("sodium");
        public static final TagKey<Fluid> SUPERHEATED_SODIUM = forgeTag("superheated_sodium");
        public static final TagKey<Fluid> STEAM = forgeTag("steam");
        public static final TagKey<Fluid> SULFUR_DIOXIDE = forgeTag("sulfur_dioxide");
        public static final TagKey<Fluid> SULFUR_TRIOXIDE = forgeTag("sulfur_trioxide");
        public static final TagKey<Fluid> SULFURIC_ACID = forgeTag("sulfuric_acid");
        public static final TagKey<Fluid> HYDROFLUORIC_ACID = forgeTag("hydrofluoric_acid");

        public static final LazyTagLookup<Fluid> WATER_LOOKUP = LazyTagLookup.create(ForgeRegistries.FLUIDS, FluidTags.WATER);
        public static final LazyTagLookup<Fluid> LAVA_LOOKUP = LazyTagLookup.create(ForgeRegistries.FLUIDS, FluidTags.LAVA);

        private static TagKey<Fluid> forgeTag(String name) {
            return FluidTags.create(new ResourceLocation("forge", name));
        }
    }

    public static class Gases {

        private static void init() {
        }

        private Gases() {
        }

        public static final TagKey<Gas> WATER_VAPOR = tag("water_vapor");
        public static final TagKey<Gas> WASTE_BARREL_DECAY_BLACKLIST = tag("waste_barrel_decay_blacklist");
        public static final LazyTagLookup<Gas> WASTE_BARREL_DECAY_LOOKUP = LazyTagLookup.create(ChemicalTags.GAS, WASTE_BARREL_DECAY_BLACKLIST);

        private static TagKey<Gas> tag(String name) {
            return ChemicalTags.GAS.tag(Mekanism.rl(name));
        }
    }

    public static class InfuseTypes {

        private static void init() {
        }

        private InfuseTypes() {
        }

        public static final TagKey<InfuseType> CARBON = tag("carbon");
        public static final TagKey<InfuseType> REDSTONE = tag("redstone");
        public static final TagKey<InfuseType> DIAMOND = tag("diamond");
        public static final TagKey<InfuseType> REFINED_OBSIDIAN = tag("refined_obsidian");
        public static final TagKey<InfuseType> BIO = tag("bio");
        public static final TagKey<InfuseType> FUNGI = tag("fungi");
        public static final TagKey<InfuseType> GOLD = tag("gold");
        public static final TagKey<InfuseType> TIN = tag("tin");

        private static TagKey<InfuseType> tag(String name) {
            return ChemicalTags.INFUSE_TYPE.tag(Mekanism.rl(name));
        }
    }

    public static class Slurries {

        private static void init() {
        }

        private Slurries() {
        }

        public static final TagKey<Slurry> DIRTY = tag("dirty");
        public static final LazyTagLookup<Slurry> DIRTY_LOOKUP = LazyTagLookup.create(ChemicalTags.SLURRY, DIRTY);
        public static final TagKey<Slurry> CLEAN = tag("clean");

        private static TagKey<Slurry> tag(String name) {
            return ChemicalTags.SLURRY.tag(Mekanism.rl(name));
        }
    }

    public static class TileEntityTypes {

        private static void init() {
        }

        private TileEntityTypes() {
        }

        public static final TagKey<BlockEntityType<?>> CARDBOARD_BLACKLIST = tag("cardboard_blacklist");
        public static final LazyTagLookup<BlockEntityType<?>> CARDBOARD_BLACKLIST_LOOKUP = LazyTagLookup.create(ForgeRegistries.BLOCK_ENTITIES, CARDBOARD_BLACKLIST);
        public static final TagKey<BlockEntityType<?>> RELOCATION_NOT_SUPPORTED = forgeTag("relocation_not_supported");
        public static final TagKey<BlockEntityType<?>> IMMOVABLE = forgeTag("immovable");

        private static TagKey<BlockEntityType<?>> tag(String name) {
            return TagUtils.createKey(ForgeRegistries.BLOCK_ENTITIES, Mekanism.rl(name));
        }

        private static TagKey<BlockEntityType<?>> forgeTag(String name) {
            return TagUtils.createKey(ForgeRegistries.BLOCK_ENTITIES, new ResourceLocation("forge", name));
        }
    }
}