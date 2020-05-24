package mekanism.common.tags;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class MekanismTags {

    public static class Items {

        public static final Table<ResourceType, PrimaryResource, Tag<Item>> PROCESSED_RESOURCES = HashBasedTable.create();
        public static final Map<PrimaryResource, Tag<Item>> PROCESSED_RESOURCE_BLOCKS = new Object2ObjectOpenHashMap<>();
        public static final Map<OreType, Tag<Item>> ORES = new Object2ObjectOpenHashMap<>();

        static {
            for (PrimaryResource resource : PrimaryResource.values()) {
                for (ResourceType type : ResourceType.values()) {
                    if (type == ResourceType.INGOT || type == ResourceType.NUGGET || type == ResourceType.DUST) {
                        PROCESSED_RESOURCES.put(type, resource, forgeTag(type.getRegistryPrefix() + "s/" + resource.getName()));
                    } else {
                        PROCESSED_RESOURCES.put(type, resource, tag(type.getRegistryPrefix() + "s/" + resource.getName()));
                    }
                }
                if (!resource.isVanilla()) {
                    PROCESSED_RESOURCE_BLOCKS.put(resource, forgeTag("storage_blocks/" + resource.getName()));
                }
            }
            for (OreType ore : OreType.values()) {
                ORES.put(ore, forgeTag("ores/" + ore.getResource().getRegistrySuffix()));
            }
        }

        public static final Tag<Item> WRENCHES = forgeTag("wrenches");

        public static final Tag<Item> BATTERIES = forgeTag("batteries");

        public static final Tag<Item> RODS_PLASTIC = forgeTag("rods/plastic");

        public static final Tag<Item> FUELS = forgeTag("fuels");
        public static final Tag<Item> FUELS_BIO = forgeTag("fuels/bio");

        public static final Tag<Item> CHESTS_ELECTRIC = forgeTag("chests/electric");
        public static final Tag<Item> CHESTS_PERSONAL = forgeTag("chests/personal");

        public static final Tag<Item> SALT = forgeTag("salt");
        public static final Tag<Item> SAWDUST = forgeTag("sawdust");
        public static final Tag<Item> YELLOW_CAKE_URANIUM = forgeTag("yellow_cake_uranium");

        public static final Tag<Item> PELLETS_ANTIMATTER = forgeTag("pellets/antimatter");
        public static final Tag<Item> PELLETS_PLUTONIUM = forgeTag("pellets/plutonium");
        public static final Tag<Item> PELLETS_POLONIUM = forgeTag("pellets/polonium");

        public static final Tag<Item> DUSTS_BRONZE = forgeTag("dusts/bronze");
        public static final Tag<Item> DUSTS_CHARCOAL = forgeTag("dusts/charcoal");
        public static final Tag<Item> DUSTS_COAL = forgeTag("dusts/coal");
        public static final Tag<Item> DUSTS_DIAMOND = forgeTag("dusts/diamond");
        public static final Tag<Item> DUSTS_EMERALD = forgeTag("dusts/emerald");
        public static final Tag<Item> DUSTS_LAPIS_LAZULI = forgeTag("dusts/lapis_lazuli");
        public static final Tag<Item> DUSTS_LITHIUM = forgeTag("dusts/lithium");
        public static final Tag<Item> DUSTS_OBSIDIAN = forgeTag("dusts/obsidian");
        public static final Tag<Item> DUSTS_QUARTZ = forgeTag("dusts/quartz");
        public static final Tag<Item> DUSTS_REFINED_OBSIDIAN = forgeTag("dusts/refined_obsidian");
        public static final Tag<Item> DUSTS_SALT = forgeTag("dusts/salt");
        public static final Tag<Item> DUSTS_STEEL = forgeTag("dusts/steel");
        public static final Tag<Item> DUSTS_SULFUR = forgeTag("dusts/sulfur");
        public static final Tag<Item> DUSTS_WOOD = forgeTag("dusts/wood");
        public static final Tag<Item> DUSTS_FLUORITE = forgeTag("dusts/fluorite");

        public static final Tag<Item> NUGGETS_BRONZE = forgeTag("nuggets/bronze");
        public static final Tag<Item> NUGGETS_REFINED_GLOWSTONE = forgeTag("nuggets/refined_glowstone");
        public static final Tag<Item> NUGGETS_REFINED_OBSIDIAN = forgeTag("nuggets/refined_obsidian");
        public static final Tag<Item> NUGGETS_STEEL = forgeTag("nuggets/steel");

        public static final Tag<Item> INGOTS_BRONZE = forgeTag("ingots/bronze");
        public static final Tag<Item> INGOTS_REFINED_GLOWSTONE = forgeTag("ingots/refined_glowstone");
        public static final Tag<Item> INGOTS_REFINED_OBSIDIAN = forgeTag("ingots/refined_obsidian");
        public static final Tag<Item> INGOTS_STEEL = forgeTag("ingots/steel");

        public static final Tag<Item> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");
        public static final Tag<Item> STORAGE_BLOCKS_CHARCOAL = forgeTag("storage_blocks/charcoal");
        public static final Tag<Item> STORAGE_BLOCKS_REFINED_GLOWSTONE = forgeTag("storage_blocks/refined_glowstone");
        public static final Tag<Item> STORAGE_BLOCKS_REFINED_OBSIDIAN = forgeTag("storage_blocks/refined_obsidian");
        public static final Tag<Item> STORAGE_BLOCKS_STEEL = forgeTag("storage_blocks/steel");

        public static final Tag<Item> CIRCUITS = forgeTag("circuits");
        public static final Tag<Item> CIRCUITS_BASIC = forgeTag("circuits/basic");
        public static final Tag<Item> CIRCUITS_ADVANCED = forgeTag("circuits/advanced");
        public static final Tag<Item> CIRCUITS_ELITE = forgeTag("circuits/elite");
        public static final Tag<Item> CIRCUITS_ULTIMATE = forgeTag("circuits/ultimate");

        public static final Tag<Item> ALLOYS = tag("alloys");
        public static final Tag<Item> ALLOYS_BASIC = tag("alloys/basic");
        public static final Tag<Item> ALLOYS_INFUSED = tag("alloys/infused");
        public static final Tag<Item> ALLOYS_REINFORCED = tag("alloys/reinforced");
        public static final Tag<Item> ALLOYS_ATOMIC = tag("alloys/atomic");
        //Forge alloy tags
        public static final Tag<Item> FORGE_ALLOYS = forgeTag("alloys");
        public static final Tag<Item> ALLOYS_ADVANCED = forgeTag("alloys/advanced");
        public static final Tag<Item> ALLOYS_ELITE = forgeTag("alloys/elite");
        public static final Tag<Item> ALLOYS_ULTIMATE = forgeTag("alloys/ultimate");

        public static final Tag<Item> ENRICHED = tag("enriched");
        public static final Tag<Item> ENRICHED_CARBON = tag("enriched/carbon");
        public static final Tag<Item> ENRICHED_DIAMOND = tag("enriched/diamond");
        public static final Tag<Item> ENRICHED_OBSIDIAN = tag("enriched/obsidian");
        public static final Tag<Item> ENRICHED_REDSTONE = tag("enriched/redstone");
        public static final Tag<Item> ENRICHED_TIN = tag("enriched/tin");

        public static final Tag<Item> DIRTY_DUSTS = tag("dirty_dusts");
        public static final Tag<Item> CLUMPS = tag("clumps");
        public static final Tag<Item> SHARDS = tag("shards");
        public static final Tag<Item> CRYSTALS = tag("crystals");

        public static final Tag<Item> GEMS_FLUORITE = forgeTag("gems/fluorite");

        private static Tag<Item> forgeTag(String name) {
            return new ItemTags.Wrapper(new ResourceLocation("forge", name));
        }

        private static Tag<Item> tag(String name) {
            return new ItemTags.Wrapper(Mekanism.rl(name));
        }
    }

    public static class Blocks {

        public static final Map<PrimaryResource, Tag<Block>> RESOURCE_STORAGE_BLOCKS = new Object2ObjectOpenHashMap<>();
        public static final Map<OreType, Tag<Block>> ORES = new Object2ObjectOpenHashMap<>();

        static {
            for (PrimaryResource resource : PrimaryResource.values()) {
                if (!resource.isVanilla()) {
                    RESOURCE_STORAGE_BLOCKS.put(resource, forgeTag("storage_blocks/" + resource.getName()));
                }
            }
            for (OreType ore : OreType.values()) {
                ORES.put(ore, forgeTag("ores/" + ore.getResource().getRegistrySuffix()));
            }
        }

        public static final Tag<Block> CARDBOARD_BLACKLIST = tag("cardboard_blacklist");
        public static final Tag<Block> ATOMIC_DISASSEMBLER_ORE = tag("atomic_disassembler_ore");

        public static final Tag<Block> CHESTS_ELECTRIC = forgeTag("chests/electric");
        public static final Tag<Block> CHESTS_PERSONAL = forgeTag("chests/personal");

        public static final Tag<Block> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");
        public static final Tag<Block> STORAGE_BLOCKS_CHARCOAL = forgeTag("storage_blocks/charcoal");
        public static final Tag<Block> STORAGE_BLOCKS_REFINED_GLOWSTONE = forgeTag("storage_blocks/refined_glowstone");
        public static final Tag<Block> STORAGE_BLOCKS_REFINED_OBSIDIAN = forgeTag("storage_blocks/refined_obsidian");
        public static final Tag<Block> STORAGE_BLOCKS_STEEL = forgeTag("storage_blocks/steel");

        private static Tag<Block> forgeTag(String name) {
            return new BlockTags.Wrapper(new ResourceLocation("forge", name));
        }

        private static Tag<Block> tag(String name) {
            return new BlockTags.Wrapper(Mekanism.rl(name));
        }
    }

    public static class Fluids {

        public static final Tag<Fluid> BRINE = forgeTag("brine");
        public static final Tag<Fluid> CHLORINE = forgeTag("chlorine");
        public static final Tag<Fluid> ETHENE = forgeTag("ethene");
        public static final Tag<Fluid> HEAVY_WATER = forgeTag("heavy_water");
        public static final Tag<Fluid> HYDROGEN = forgeTag("hydrogen");
        public static final Tag<Fluid> HYDROGEN_CHLORIDE = forgeTag("hydrogen_chloride");
        public static final Tag<Fluid> LITHIUM = forgeTag("lithium");
        public static final Tag<Fluid> OXYGEN = forgeTag("oxygen");
        public static final Tag<Fluid> SODIUM = forgeTag("sodium");
        public static final Tag<Fluid> STEAM = forgeTag("steam");
        public static final Tag<Fluid> SULFUR_DIOXIDE = forgeTag("sulfur_dioxide");
        public static final Tag<Fluid> SULFUR_TRIOXIDE = forgeTag("sulfur_trioxide");
        public static final Tag<Fluid> SULFURIC_ACID = forgeTag("sulfuric_acid");
        public static final Tag<Fluid> HYDROFLUORIC_ACID = forgeTag("hydrofluoric_acid");

        private static Tag<Fluid> forgeTag(String name) {
            return new FluidTags.Wrapper(new ResourceLocation("forge", name));
        }
    }

    public static class Gases {

        public static final Tag<Gas> DIRTY_SLURRY = tag("dirty_slurry");
        public static final Tag<Gas> CLEAN_SLURRY = tag("clean_slurry");
        public static final Tag<Gas> WATER_VAPOR = tag("water_vapor");

        private static Tag<Gas> tag(String name) {
            return ChemicalTags.gasTag(Mekanism.rl(name));
        }
    }

    public static class InfuseTypes {

        public static final Tag<InfuseType> CARBON = tag("carbon");
        public static final Tag<InfuseType> REDSTONE = tag("redstone");
        public static final Tag<InfuseType> DIAMOND = tag("diamond");
        public static final Tag<InfuseType> REFINED_OBSIDIAN = tag("refined_obsidian");
        public static final Tag<InfuseType> BIO = tag("bio");
        public static final Tag<InfuseType> FUNGI = tag("fungi");
        public static final Tag<InfuseType> TIN = tag("tin");

        private static Tag<InfuseType> tag(String name) {
            return ChemicalTags.infusionTag(Mekanism.rl(name));
        }
    }

    public static class Pigments {

        private static Tag<Pigment> tag(String name) {
            return ChemicalTags.pigmentTag(Mekanism.rl(name));
        }
    }

    public static class Slurries {

        public static final Tag<Slurry> DIRTY = tag("dirty");
        public static final Tag<Slurry> CLEAN = tag("clean");

        private static Tag<Slurry> tag(String name) {
            return ChemicalTags.slurryTag(Mekanism.rl(name));
        }
    }
}