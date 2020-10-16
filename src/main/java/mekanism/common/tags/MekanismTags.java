package mekanism.common.tags;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.EnumMap;
import java.util.Map;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.Mekanism;
import mekanism.common.resource.OreType;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class MekanismTags {

    private MekanismTags() {
    }

    public static class Items {

        private Items() {
        }

        public static final Table<ResourceType, PrimaryResource, INamedTag<Item>> PROCESSED_RESOURCES = HashBasedTable.create();
        public static final Map<PrimaryResource, INamedTag<Item>> PROCESSED_RESOURCE_BLOCKS = new EnumMap<>(PrimaryResource.class);
        public static final Map<OreType, INamedTag<Item>> ORES = new EnumMap<>(OreType.class);

        static {
            for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
                for (ResourceType type : EnumUtils.RESOURCE_TYPES) {
                    if (type.usedByPrimary()) {
                        if (type == ResourceType.INGOT || type == ResourceType.NUGGET || type == ResourceType.DUST) {
                            PROCESSED_RESOURCES.put(type, resource, forgeTag(type.getPluralPrefix() + "/" + resource.getName()));
                        } else {
                            PROCESSED_RESOURCES.put(type, resource, tag(type.getPluralPrefix() + "/" + resource.getName()));
                        }
                    }
                }
                if (!resource.isVanilla()) {
                    PROCESSED_RESOURCE_BLOCKS.put(resource, forgeTag("storage_blocks/" + resource.getName()));
                }
            }
            for (OreType ore : EnumUtils.ORE_TYPES) {
                ORES.put(ore, forgeTag("ores/" + ore.getResource().getRegistrySuffix()));
            }
        }

        public static final INamedTag<Item> CONFIGURATORS = tag("configurators");
        public static final INamedTag<Item> WRENCHES = forgeTag("wrenches");
        public static final INamedTag<Item> TOOLS = forgeTag("tools");
        public static final INamedTag<Item> TOOLS_WRENCHES = forgeTag("tools/wrenches");

        public static final INamedTag<Item> BATTERIES = forgeTag("batteries");

        public static final INamedTag<Item> RODS_PLASTIC = forgeTag("rods/plastic");

        public static final INamedTag<Item> FUELS = forgeTag("fuels");
        public static final INamedTag<Item> FUELS_BIO = forgeTag("fuels/bio");

        public static final INamedTag<Item> CHESTS_ELECTRIC = forgeTag("chests/electric");
        public static final INamedTag<Item> CHESTS_PERSONAL = forgeTag("chests/personal");

        public static final INamedTag<Item> SALT = forgeTag("salt");
        public static final INamedTag<Item> SAWDUST = forgeTag("sawdust");
        public static final INamedTag<Item> YELLOW_CAKE_URANIUM = forgeTag("yellow_cake_uranium");

        public static final INamedTag<Item> PELLETS_ANTIMATTER = forgeTag("pellets/antimatter");
        public static final INamedTag<Item> PELLETS_PLUTONIUM = forgeTag("pellets/plutonium");
        public static final INamedTag<Item> PELLETS_POLONIUM = forgeTag("pellets/polonium");

        public static final INamedTag<Item> DUSTS_BRONZE = forgeTag("dusts/bronze");
        public static final INamedTag<Item> DUSTS_CHARCOAL = forgeTag("dusts/charcoal");
        public static final INamedTag<Item> DUSTS_COAL = forgeTag("dusts/coal");
        public static final INamedTag<Item> DUSTS_DIAMOND = forgeTag("dusts/diamond");
        public static final INamedTag<Item> DUSTS_EMERALD = forgeTag("dusts/emerald");
        public static final INamedTag<Item> DUSTS_NETHERITE = forgeTag("dusts/netherite");
        public static final INamedTag<Item> DUSTS_LAPIS = forgeTag("dusts/lapis");
        public static final INamedTag<Item> DUSTS_LITHIUM = forgeTag("dusts/lithium");
        public static final INamedTag<Item> DUSTS_OBSIDIAN = forgeTag("dusts/obsidian");
        public static final INamedTag<Item> DUSTS_QUARTZ = forgeTag("dusts/quartz");
        public static final INamedTag<Item> DUSTS_REFINED_OBSIDIAN = forgeTag("dusts/refined_obsidian");
        public static final INamedTag<Item> DUSTS_SALT = forgeTag("dusts/salt");
        public static final INamedTag<Item> DUSTS_STEEL = forgeTag("dusts/steel");
        public static final INamedTag<Item> DUSTS_SULFUR = forgeTag("dusts/sulfur");
        public static final INamedTag<Item> DUSTS_WOOD = forgeTag("dusts/wood");
        public static final INamedTag<Item> DUSTS_FLUORITE = forgeTag("dusts/fluorite");

        public static final INamedTag<Item> NUGGETS_BRONZE = forgeTag("nuggets/bronze");
        public static final INamedTag<Item> NUGGETS_REFINED_GLOWSTONE = forgeTag("nuggets/refined_glowstone");
        public static final INamedTag<Item> NUGGETS_REFINED_OBSIDIAN = forgeTag("nuggets/refined_obsidian");
        public static final INamedTag<Item> NUGGETS_STEEL = forgeTag("nuggets/steel");

        public static final INamedTag<Item> INGOTS_BRONZE = forgeTag("ingots/bronze");
        public static final INamedTag<Item> INGOTS_REFINED_GLOWSTONE = forgeTag("ingots/refined_glowstone");
        public static final INamedTag<Item> INGOTS_REFINED_OBSIDIAN = forgeTag("ingots/refined_obsidian");
        public static final INamedTag<Item> INGOTS_STEEL = forgeTag("ingots/steel");

        public static final INamedTag<Item> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");
        public static final INamedTag<Item> STORAGE_BLOCKS_CHARCOAL = forgeTag("storage_blocks/charcoal");
        public static final INamedTag<Item> STORAGE_BLOCKS_REFINED_GLOWSTONE = forgeTag("storage_blocks/refined_glowstone");
        public static final INamedTag<Item> STORAGE_BLOCKS_REFINED_OBSIDIAN = forgeTag("storage_blocks/refined_obsidian");
        public static final INamedTag<Item> STORAGE_BLOCKS_STEEL = forgeTag("storage_blocks/steel");

        public static final INamedTag<Item> CIRCUITS = forgeTag("circuits");
        public static final INamedTag<Item> CIRCUITS_BASIC = forgeTag("circuits/basic");
        public static final INamedTag<Item> CIRCUITS_ADVANCED = forgeTag("circuits/advanced");
        public static final INamedTag<Item> CIRCUITS_ELITE = forgeTag("circuits/elite");
        public static final INamedTag<Item> CIRCUITS_ULTIMATE = forgeTag("circuits/ultimate");

        public static final INamedTag<Item> ALLOYS = tag("alloys");
        public static final INamedTag<Item> ALLOYS_BASIC = tag("alloys/basic");
        public static final INamedTag<Item> ALLOYS_INFUSED = tag("alloys/infused");
        public static final INamedTag<Item> ALLOYS_REINFORCED = tag("alloys/reinforced");
        public static final INamedTag<Item> ALLOYS_ATOMIC = tag("alloys/atomic");
        //Forge alloy tags
        public static final INamedTag<Item> FORGE_ALLOYS = forgeTag("alloys");
        public static final INamedTag<Item> ALLOYS_ADVANCED = forgeTag("alloys/advanced");
        public static final INamedTag<Item> ALLOYS_ELITE = forgeTag("alloys/elite");
        public static final INamedTag<Item> ALLOYS_ULTIMATE = forgeTag("alloys/ultimate");

        public static final INamedTag<Item> ENRICHED = tag("enriched");
        public static final INamedTag<Item> ENRICHED_CARBON = tag("enriched/carbon");
        public static final INamedTag<Item> ENRICHED_DIAMOND = tag("enriched/diamond");
        public static final INamedTag<Item> ENRICHED_OBSIDIAN = tag("enriched/obsidian");
        public static final INamedTag<Item> ENRICHED_REDSTONE = tag("enriched/redstone");
        public static final INamedTag<Item> ENRICHED_GOLD = tag("enriched/gold");
        public static final INamedTag<Item> ENRICHED_TIN = tag("enriched/tin");

        public static final INamedTag<Item> DIRTY_DUSTS = tag("dirty_dusts");
        public static final INamedTag<Item> CLUMPS = tag("clumps");
        public static final INamedTag<Item> SHARDS = tag("shards");
        public static final INamedTag<Item> CRYSTALS = tag("crystals");

        public static final INamedTag<Item> GEMS_FLUORITE = forgeTag("gems/fluorite");

        private static INamedTag<Item> forgeTag(String name) {
            return ItemTags.makeWrapperTag("forge:" + name);
        }

        private static INamedTag<Item> tag(String name) {
            return ItemTags.makeWrapperTag(Mekanism.rl(name).toString());
        }
    }

    public static class Blocks {

        private Blocks() {
        }

        public static final Map<PrimaryResource, INamedTag<Block>> RESOURCE_STORAGE_BLOCKS = new EnumMap<>(PrimaryResource.class);
        public static final Map<OreType, INamedTag<Block>> ORES = new EnumMap<>(OreType.class);

        static {
            for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
                if (!resource.isVanilla()) {
                    RESOURCE_STORAGE_BLOCKS.put(resource, forgeTag("storage_blocks/" + resource.getName()));
                }
            }
            for (OreType ore : EnumUtils.ORE_TYPES) {
                ORES.put(ore, forgeTag("ores/" + ore.getResource().getRegistrySuffix()));
            }
        }

        public static final INamedTag<Block> RELOCATION_NOT_SUPPORTED = forgeTag("relocation_not_supported");
        public static final INamedTag<Block> CARDBOARD_BLACKLIST = tag("cardboard_blacklist");
        public static final INamedTag<Block> ATOMIC_DISASSEMBLER_ORE = tag("atomic_disassembler_ore");

        public static final INamedTag<Block> CHESTS_ELECTRIC = forgeTag("chests/electric");
        public static final INamedTag<Block> CHESTS_PERSONAL = forgeTag("chests/personal");

        public static final INamedTag<Block> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");
        public static final INamedTag<Block> STORAGE_BLOCKS_CHARCOAL = forgeTag("storage_blocks/charcoal");
        public static final INamedTag<Block> STORAGE_BLOCKS_REFINED_GLOWSTONE = forgeTag("storage_blocks/refined_glowstone");
        public static final INamedTag<Block> STORAGE_BLOCKS_REFINED_OBSIDIAN = forgeTag("storage_blocks/refined_obsidian");
        public static final INamedTag<Block> STORAGE_BLOCKS_STEEL = forgeTag("storage_blocks/steel");

        private static INamedTag<Block> forgeTag(String name) {
            return BlockTags.makeWrapperTag("forge:" + name);
        }

        private static INamedTag<Block> tag(String name) {
            return BlockTags.makeWrapperTag(Mekanism.rl(name).toString());
        }
    }

    public static class Fluids {

        private Fluids() {
        }

        public static final INamedTag<Fluid> BRINE = forgeTag("brine");
        public static final INamedTag<Fluid> CHLORINE = forgeTag("chlorine");
        public static final INamedTag<Fluid> ETHENE = forgeTag("ethene");
        public static final INamedTag<Fluid> HEAVY_WATER = forgeTag("heavy_water");
        public static final INamedTag<Fluid> HYDROGEN = forgeTag("hydrogen");
        public static final INamedTag<Fluid> HYDROGEN_CHLORIDE = forgeTag("hydrogen_chloride");
        public static final INamedTag<Fluid> LITHIUM = forgeTag("lithium");
        public static final INamedTag<Fluid> OXYGEN = forgeTag("oxygen");
        public static final INamedTag<Fluid> SODIUM = forgeTag("sodium");
        public static final INamedTag<Fluid> STEAM = forgeTag("steam");
        public static final INamedTag<Fluid> SULFUR_DIOXIDE = forgeTag("sulfur_dioxide");
        public static final INamedTag<Fluid> SULFUR_TRIOXIDE = forgeTag("sulfur_trioxide");
        public static final INamedTag<Fluid> SULFURIC_ACID = forgeTag("sulfuric_acid");
        public static final INamedTag<Fluid> HYDROFLUORIC_ACID = forgeTag("hydrofluoric_acid");

        private static INamedTag<Fluid> forgeTag(String name) {
            return FluidTags.makeWrapperTag("forge:" + name);
        }
    }

    public static class Gases {

        private Gases() {
        }

        public static final INamedTag<Gas> WATER_VAPOR = tag("water_vapor");

        private static INamedTag<Gas> tag(String name) {
            return ChemicalTags.GAS.tag(Mekanism.rl(name));
        }
    }

    public static class InfuseTypes {

        private InfuseTypes() {
        }

        public static final INamedTag<InfuseType> CARBON = tag("carbon");
        public static final INamedTag<InfuseType> REDSTONE = tag("redstone");
        public static final INamedTag<InfuseType> DIAMOND = tag("diamond");
        public static final INamedTag<InfuseType> REFINED_OBSIDIAN = tag("refined_obsidian");
        public static final INamedTag<InfuseType> BIO = tag("bio");
        public static final INamedTag<InfuseType> FUNGI = tag("fungi");
        public static final INamedTag<InfuseType> GOLD = tag("gold");
        public static final INamedTag<InfuseType> TIN = tag("tin");

        private static INamedTag<InfuseType> tag(String name) {
            return ChemicalTags.INFUSE_TYPE.tag(Mekanism.rl(name));
        }
    }

    public static class Pigments {

        private Pigments() {
        }

        private static INamedTag<Pigment> tag(String name) {
            return ChemicalTags.PIGMENT.tag(Mekanism.rl(name));
        }
    }

    public static class Slurries {

        private Slurries() {
        }

        public static final INamedTag<Slurry> DIRTY = tag("dirty");
        public static final INamedTag<Slurry> CLEAN = tag("clean");

        private static INamedTag<Slurry> tag(String name) {
            return ChemicalTags.SLURRY.tag(Mekanism.rl(name));
        }
    }

    public static class TileEntityTypes {

        private TileEntityTypes() {
        }

        public static final INamedTag<TileEntityType<?>> CARDBOARD_BLACKLIST = tag("cardboard_blacklist");
        public static final INamedTag<TileEntityType<?>> RELOCATION_NOT_SUPPORTED = forgeTag("relocation_not_supported");
        public static final INamedTag<TileEntityType<?>> IMMOVABLE = forgeTag("immovable");

        private static INamedTag<TileEntityType<?>> tag(String name) {
            return ForgeTagHandler.makeWrapperTag(ForgeRegistries.TILE_ENTITIES, Mekanism.rl(name));
        }

        private static INamedTag<TileEntityType<?>> forgeTag(String name) {
            return ForgeTagHandler.makeWrapperTag(ForgeRegistries.TILE_ENTITIES, new ResourceLocation("forge", name));
        }
    }
}