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
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class MekanismTags {

    /**
     * Call to force make sure this is all initialized
     */
    public static void init() {
        Items.init();
        Blocks.init();
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

        public static final Table<ResourceType, PrimaryResource, Named<Item>> PROCESSED_RESOURCES = HashBasedTable.create();
        public static final Map<IResource, Named<Item>> PROCESSED_RESOURCE_BLOCKS = new HashMap<>();
        public static final Map<OreType, Named<Item>> ORES = new EnumMap<>(OreType.class);

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

        public static final Named<Item> CONFIGURATORS = tag("configurators");
        public static final Named<Item> WRENCHES = forgeTag("wrenches");
        public static final Named<Item> TOOLS = forgeTag("tools");
        public static final Named<Item> TOOLS_WRENCH = forgeTag("tools/wrench");

        public static final Named<Item> BATTERIES = forgeTag("batteries");

        public static final Named<Item> RODS_PLASTIC = forgeTag("rods/plastic");

        public static final Named<Item> FUELS = forgeTag("fuels");
        public static final Named<Item> FUELS_BIO = forgeTag("fuels/bio");

        public static final Named<Item> SALT = forgeTag("salt");
        public static final Named<Item> SAWDUST = forgeTag("sawdust");
        public static final Named<Item> YELLOW_CAKE_URANIUM = forgeTag("yellow_cake_uranium");

        public static final Named<Item> PELLETS_ANTIMATTER = forgeTag("pellets/antimatter");
        public static final Named<Item> PELLETS_PLUTONIUM = forgeTag("pellets/plutonium");
        public static final Named<Item> PELLETS_POLONIUM = forgeTag("pellets/polonium");

        public static final Named<Item> DUSTS_BRONZE = forgeTag("dusts/bronze");
        public static final Named<Item> DUSTS_CHARCOAL = forgeTag("dusts/charcoal");
        public static final Named<Item> DUSTS_COAL = forgeTag("dusts/coal");
        public static final Named<Item> DUSTS_DIAMOND = forgeTag("dusts/diamond");
        public static final Named<Item> DUSTS_EMERALD = forgeTag("dusts/emerald");
        public static final Named<Item> DUSTS_NETHERITE = forgeTag("dusts/netherite");
        public static final Named<Item> DUSTS_LAPIS = forgeTag("dusts/lapis");
        public static final Named<Item> DUSTS_LITHIUM = forgeTag("dusts/lithium");
        public static final Named<Item> DUSTS_OBSIDIAN = forgeTag("dusts/obsidian");
        public static final Named<Item> DUSTS_QUARTZ = forgeTag("dusts/quartz");
        public static final Named<Item> DUSTS_REFINED_OBSIDIAN = forgeTag("dusts/refined_obsidian");
        public static final Named<Item> DUSTS_SALT = forgeTag("dusts/salt");
        public static final Named<Item> DUSTS_STEEL = forgeTag("dusts/steel");
        public static final Named<Item> DUSTS_SULFUR = forgeTag("dusts/sulfur");
        public static final Named<Item> DUSTS_WOOD = forgeTag("dusts/wood");
        public static final Named<Item> DUSTS_FLUORITE = forgeTag("dusts/fluorite");

        public static final Named<Item> NUGGETS_BRONZE = forgeTag("nuggets/bronze");
        public static final Named<Item> NUGGETS_REFINED_GLOWSTONE = forgeTag("nuggets/refined_glowstone");
        public static final Named<Item> NUGGETS_REFINED_OBSIDIAN = forgeTag("nuggets/refined_obsidian");
        public static final Named<Item> NUGGETS_STEEL = forgeTag("nuggets/steel");

        public static final Named<Item> INGOTS_BRONZE = forgeTag("ingots/bronze");
        public static final Named<Item> INGOTS_REFINED_GLOWSTONE = forgeTag("ingots/refined_glowstone");
        public static final Named<Item> INGOTS_REFINED_OBSIDIAN = forgeTag("ingots/refined_obsidian");
        public static final Named<Item> INGOTS_STEEL = forgeTag("ingots/steel");

        public static final Named<Item> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");
        public static final Named<Item> STORAGE_BLOCKS_CHARCOAL = forgeTag("storage_blocks/charcoal");
        public static final Named<Item> STORAGE_BLOCKS_REFINED_GLOWSTONE = forgeTag("storage_blocks/refined_glowstone");
        public static final Named<Item> STORAGE_BLOCKS_REFINED_OBSIDIAN = forgeTag("storage_blocks/refined_obsidian");
        public static final Named<Item> STORAGE_BLOCKS_STEEL = forgeTag("storage_blocks/steel");
        public static final Named<Item> STORAGE_BLOCKS_FLUORITE = forgeTag("storage_blocks/fluorite");

        public static final Named<Item> CIRCUITS = forgeTag("circuits");
        public static final Named<Item> CIRCUITS_BASIC = forgeTag("circuits/basic");
        public static final Named<Item> CIRCUITS_ADVANCED = forgeTag("circuits/advanced");
        public static final Named<Item> CIRCUITS_ELITE = forgeTag("circuits/elite");
        public static final Named<Item> CIRCUITS_ULTIMATE = forgeTag("circuits/ultimate");

        public static final Named<Item> ALLOYS = tag("alloys");
        public static final Named<Item> ALLOYS_BASIC = tag("alloys/basic");
        public static final Named<Item> ALLOYS_INFUSED = tag("alloys/infused");
        public static final Named<Item> ALLOYS_REINFORCED = tag("alloys/reinforced");
        public static final Named<Item> ALLOYS_ATOMIC = tag("alloys/atomic");
        //Forge alloy tags
        public static final Named<Item> FORGE_ALLOYS = forgeTag("alloys");
        public static final Named<Item> ALLOYS_ADVANCED = forgeTag("alloys/advanced");
        public static final Named<Item> ALLOYS_ELITE = forgeTag("alloys/elite");
        public static final Named<Item> ALLOYS_ULTIMATE = forgeTag("alloys/ultimate");

        public static final Named<Item> ENRICHED = tag("enriched");
        public static final Named<Item> ENRICHED_CARBON = tag("enriched/carbon");
        public static final Named<Item> ENRICHED_DIAMOND = tag("enriched/diamond");
        public static final Named<Item> ENRICHED_OBSIDIAN = tag("enriched/obsidian");
        public static final Named<Item> ENRICHED_REDSTONE = tag("enriched/redstone");
        public static final Named<Item> ENRICHED_GOLD = tag("enriched/gold");
        public static final Named<Item> ENRICHED_TIN = tag("enriched/tin");

        public static final Named<Item> DIRTY_DUSTS = tag("dirty_dusts");
        public static final Named<Item> CLUMPS = tag("clumps");
        public static final Named<Item> SHARDS = tag("shards");
        public static final Named<Item> CRYSTALS = tag("crystals");

        public static final Named<Item> GEMS_FLUORITE = forgeTag("gems/fluorite");

        public static final Named<Item> COLORABLE_WOOL = tag("colorable/wool");
        public static final Named<Item> COLORABLE_CARPETS = tag("colorable/carpets");
        public static final Named<Item> COLORABLE_BEDS = tag("colorable/beds");
        public static final Named<Item> COLORABLE_GLASS = tag("colorable/glass");
        public static final Named<Item> COLORABLE_GLASS_PANES = tag("colorable/glass_panes");
        public static final Named<Item> COLORABLE_TERRACOTTA = tag("colorable/terracotta");
        public static final Named<Item> COLORABLE_CONCRETE = tag("colorable/concrete");
        public static final Named<Item> COLORABLE_CONCRETE_POWDER = tag("colorable/concrete_powder");
        public static final Named<Item> COLORABLE_BANNERS = tag("colorable/banners");

        private static Named<Item> forgeTag(String name) {
            return ItemTags.bind("forge:" + name);
        }

        private static Named<Item> tag(String name) {
            return ItemTags.bind(Mekanism.rl(name).toString());
        }
    }

    public static class Blocks {

        private static void init() {
        }

        private Blocks() {
        }

        public static final Map<IResource, Named<Block>> RESOURCE_STORAGE_BLOCKS = new HashMap<>();
        public static final Map<OreType, Named<Block>> ORES = new EnumMap<>(OreType.class);

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

        public static final Named<Block> RELOCATION_NOT_SUPPORTED = forgeTag("relocation_not_supported");
        public static final Named<Block> CARDBOARD_BLACKLIST = tag("cardboard_blacklist");
        public static final Named<Block> MINER_BLACKLIST = tag("miner_blacklist");
        public static final Named<Block> ATOMIC_DISASSEMBLER_ORE = tag("atomic_disassembler_ore");

        public static final Named<Block> CHESTS_ELECTRIC = forgeTag("chests/electric");
        public static final Named<Block> CHESTS_PERSONAL = forgeTag("chests/personal");

        public static final Named<Block> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");
        public static final Named<Block> STORAGE_BLOCKS_CHARCOAL = forgeTag("storage_blocks/charcoal");
        public static final Named<Block> STORAGE_BLOCKS_REFINED_GLOWSTONE = forgeTag("storage_blocks/refined_glowstone");
        public static final Named<Block> STORAGE_BLOCKS_REFINED_OBSIDIAN = forgeTag("storage_blocks/refined_obsidian");
        public static final Named<Block> STORAGE_BLOCKS_STEEL = forgeTag("storage_blocks/steel");
        public static final Named<Block> STORAGE_BLOCKS_FLUORITE = forgeTag("storage_blocks/fluorite");

        private static Named<Block> forgeTag(String name) {
            return BlockTags.bind("forge:" + name);
        }

        private static Named<Block> tag(String name) {
            return BlockTags.bind(Mekanism.rl(name).toString());
        }
    }

    public static class Fluids {

        private static void init() {
        }

        private Fluids() {
        }

        public static final Named<Fluid> BRINE = forgeTag("brine");
        public static final Named<Fluid> CHLORINE = forgeTag("chlorine");
        public static final Named<Fluid> ETHENE = forgeTag("ethene");
        public static final Named<Fluid> HEAVY_WATER = forgeTag("heavy_water");
        public static final Named<Fluid> HYDROGEN = forgeTag("hydrogen");
        public static final Named<Fluid> HYDROGEN_CHLORIDE = forgeTag("hydrogen_chloride");
        public static final Named<Fluid> URANIUM_OXIDE = forgeTag("uranium_oxide");
        public static final Named<Fluid> URANIUM_HEXAFLUORIDE = forgeTag("uranium_hexafluoride");
        public static final Named<Fluid> LITHIUM = forgeTag("lithium");
        public static final Named<Fluid> OXYGEN = forgeTag("oxygen");
        public static final Named<Fluid> SODIUM = forgeTag("sodium");
        public static final Named<Fluid> SUPERHEATED_SODIUM = forgeTag("superheated_sodium");
        public static final Named<Fluid> STEAM = forgeTag("steam");
        public static final Named<Fluid> SULFUR_DIOXIDE = forgeTag("sulfur_dioxide");
        public static final Named<Fluid> SULFUR_TRIOXIDE = forgeTag("sulfur_trioxide");
        public static final Named<Fluid> SULFURIC_ACID = forgeTag("sulfuric_acid");
        public static final Named<Fluid> HYDROFLUORIC_ACID = forgeTag("hydrofluoric_acid");

        private static Named<Fluid> forgeTag(String name) {
            return FluidTags.bind("forge:" + name);
        }
    }

    public static class Gases {

        private static void init() {
        }

        private Gases() {
        }

        public static final Named<Gas> WATER_VAPOR = tag("water_vapor");
        public static final Named<Gas> WASTE_BARREL_DECAY_BLACKLIST = tag("waste_barrel_decay_blacklist");

        private static Named<Gas> tag(String name) {
            return ChemicalTags.GAS.tag(Mekanism.rl(name));
        }
    }

    public static class InfuseTypes {

        private static void init() {
        }

        private InfuseTypes() {
        }

        public static final Named<InfuseType> CARBON = tag("carbon");
        public static final Named<InfuseType> REDSTONE = tag("redstone");
        public static final Named<InfuseType> DIAMOND = tag("diamond");
        public static final Named<InfuseType> REFINED_OBSIDIAN = tag("refined_obsidian");
        public static final Named<InfuseType> BIO = tag("bio");
        public static final Named<InfuseType> FUNGI = tag("fungi");
        public static final Named<InfuseType> GOLD = tag("gold");
        public static final Named<InfuseType> TIN = tag("tin");

        private static Named<InfuseType> tag(String name) {
            return ChemicalTags.INFUSE_TYPE.tag(Mekanism.rl(name));
        }
    }

    public static class Slurries {

        private static void init() {
        }

        private Slurries() {
        }

        public static final Named<Slurry> DIRTY = tag("dirty");
        public static final Named<Slurry> CLEAN = tag("clean");

        private static Named<Slurry> tag(String name) {
            return ChemicalTags.SLURRY.tag(Mekanism.rl(name));
        }
    }

    public static class TileEntityTypes {

        private static void init() {
        }

        private TileEntityTypes() {
        }

        public static final Named<BlockEntityType<?>> CARDBOARD_BLACKLIST = tag("cardboard_blacklist");
        public static final Named<BlockEntityType<?>> RELOCATION_NOT_SUPPORTED = forgeTag("relocation_not_supported");
        public static final Named<BlockEntityType<?>> IMMOVABLE = forgeTag("immovable");

        private static Named<BlockEntityType<?>> tag(String name) {
            return ForgeTagHandler.makeWrapperTag(ForgeRegistries.BLOCK_ENTITIES, Mekanism.rl(name));
        }

        private static Named<BlockEntityType<?>> forgeTag(String name) {
            return ForgeTagHandler.makeWrapperTag(ForgeRegistries.BLOCK_ENTITIES, new ResourceLocation("forge", name));
        }
    }
}