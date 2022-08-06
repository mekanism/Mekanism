package mekanism.tools.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ToolsTags {

    /**
     * Call to force make sure this is all initialized
     */
    public static void init() {
        Blocks.init();
        Items.init();
    }

    private ToolsTags() {
    }

    public static class Blocks {

        private static void init() {
        }

        private Blocks() {
        }

        public static final TagKey<Block> MINEABLE_WITH_PAXEL = forgeTag("mineable/paxel");
        public static final TagKey<Block> NEEDS_BRONZE_TOOL = tag("needs_bronze_tool");
        public static final TagKey<Block> NEEDS_LAPIS_LAZULI_TOOL = tag("needs_lapis_lazuli_tool");
        public static final TagKey<Block> NEEDS_OSMIUM_TOOL = tag("needs_osmium_tool");
        public static final TagKey<Block> NEEDS_REFINED_GLOWSTONE_TOOL = tag("needs_refined_glowstone_tool");
        public static final TagKey<Block> NEEDS_REFINED_OBSIDIAN_TOOL = tag("needs_refined_obsidian_tool");
        public static final TagKey<Block> NEEDS_STEEL_TOOL = tag("needs_steel_tool");

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(MekanismTools.rl(name));
        }
    }

    public static class Items {

        private static void init() {
        }

        private Items() {
        }

        public static final TagKey<Item> TOOLS_PAXELS = forgeTag("tools/paxels");

        public static final TagKey<Item> TOOLS_PAXELS_WOOD = forgeTag("tools/paxels/wood");
        public static final TagKey<Item> TOOLS_PAXELS_STONE = forgeTag("tools/paxels/stone");
        public static final TagKey<Item> TOOLS_PAXELS_GOLD = forgeTag("tools/paxels/gold");
        public static final TagKey<Item> TOOLS_PAXELS_IRON = forgeTag("tools/paxels/iron");
        public static final TagKey<Item> TOOLS_PAXELS_DIAMOND = forgeTag("tools/paxels/diamond");
        public static final TagKey<Item> TOOLS_PAXELS_NETHERITE = forgeTag("tools/paxels/netherite");
        
        public static final TagKey<Item> TOOLS_PAXELS_BRONZE = forgeTag("tools/paxels/bronze");
        public static final TagKey<Item> TOOLS_PAXELS_LAPIS_LAZULI = forgeTag("tools/paxels/lapis_lazuli");
        public static final TagKey<Item> TOOLS_PAXELS_OSMIUM = forgeTag("tools/paxels/osmium");
        public static final TagKey<Item> TOOLS_PAXELS_REFINED_GLOWSTONE = forgeTag("tools/paxels/refined_glowstone");
        public static final TagKey<Item> TOOLS_PAXELS_REFINED_OBSIDIAN = forgeTag("tools/paxels/refined_obsidian");
        public static final TagKey<Item> TOOLS_PAXELS_STEEL = forgeTag("tools/paxels/steel");

        public static final TagKey<Item> TOOLS_SWORDS_BRONZE = forgeTag("tools/swords/bronze");
        public static final TagKey<Item> TOOLS_SWORDS_LAPIS_LAZULI = forgeTag("tools/swords/lapis_lazuli");
        public static final TagKey<Item> TOOLS_SWORDS_OSMIUM = forgeTag("tools/swords/osmium");
        public static final TagKey<Item> TOOLS_SWORDS_REFINED_GLOWSTONE = forgeTag("tools/swords/refined_glowstone");
        public static final TagKey<Item> TOOLS_SWORDS_REFINED_OBSIDIAN = forgeTag("tools/swords/refined_obsidian");
        public static final TagKey<Item> TOOLS_SWORDS_STEEL = forgeTag("tools/swords/steel");

        public static final TagKey<Item> TOOLS_AXES_BRONZE = forgeTag("tools/axes/bronze");
        public static final TagKey<Item> TOOLS_AXES_LAPIS_LAZULI = forgeTag("tools/axes/lapis_lazuli");
        public static final TagKey<Item> TOOLS_AXES_OSMIUM = forgeTag("tools/axes/osmium");
        public static final TagKey<Item> TOOLS_AXES_REFINED_GLOWSTONE = forgeTag("tools/axes/refined_glowstone");
        public static final TagKey<Item> TOOLS_AXES_REFINED_OBSIDIAN = forgeTag("tools/axes/refined_obsidian");
        public static final TagKey<Item> TOOLS_AXES_STEEL = forgeTag("tools/axes/steel");

        public static final TagKey<Item> TOOLS_PICKAXES_BRONZE = forgeTag("tools/pickaxes/bronze");
        public static final TagKey<Item> TOOLS_PICKAXES_LAPIS_LAZULI = forgeTag("tools/pickaxes/lapis_lazuli");
        public static final TagKey<Item> TOOLS_PICKAXES_OSMIUM = forgeTag("tools/pickaxes/osmium");
        public static final TagKey<Item> TOOLS_PICKAXES_REFINED_GLOWSTONE = forgeTag("tools/pickaxes/refined_glowstone");
        public static final TagKey<Item> TOOLS_PICKAXES_REFINED_OBSIDIAN = forgeTag("tools/pickaxes/refined_obsidian");
        public static final TagKey<Item> TOOLS_PICKAXES_STEEL = forgeTag("tools/pickaxes/steel");

        public static final TagKey<Item> TOOLS_SHOVELS_BRONZE = forgeTag("tools/shovels/bronze");
        public static final TagKey<Item> TOOLS_SHOVELS_LAPIS_LAZULI = forgeTag("tools/shovels/lapis_lazuli");
        public static final TagKey<Item> TOOLS_SHOVELS_OSMIUM = forgeTag("tools/shovels/osmium");
        public static final TagKey<Item> TOOLS_SHOVELS_REFINED_GLOWSTONE = forgeTag("tools/shovels/refined_glowstone");
        public static final TagKey<Item> TOOLS_SHOVELS_REFINED_OBSIDIAN = forgeTag("tools/shovels/refined_obsidian");
        public static final TagKey<Item> TOOLS_SHOVELS_STEEL = forgeTag("tools/shovels/steel");

        public static final TagKey<Item> TOOLS_HOES_BRONZE = forgeTag("tools/hoes/bronze");
        public static final TagKey<Item> TOOLS_HOES_LAPIS_LAZULI = forgeTag("tools/hoes/lapis_lazuli");
        public static final TagKey<Item> TOOLS_HOES_OSMIUM = forgeTag("tools/hoes/osmium");
        public static final TagKey<Item> TOOLS_HOES_REFINED_GLOWSTONE = forgeTag("tools/hoes/refined_glowstone");
        public static final TagKey<Item> TOOLS_HOES_REFINED_OBSIDIAN = forgeTag("tools/hoes/refined_obsidian");
        public static final TagKey<Item> TOOLS_HOES_STEEL = forgeTag("tools/hoes/steel");

        public static final TagKey<Item> TOOLS_SHIELDS_BRONZE = forgeTag("tools/shields/bronze");
        public static final TagKey<Item> TOOLS_SHIELDS_LAPIS_LAZULI = forgeTag("tools/shields/lapis_lazuli");
        public static final TagKey<Item> TOOLS_SHIELDS_OSMIUM = forgeTag("tools/shields/osmium");
        public static final TagKey<Item> TOOLS_SHIELDS_REFINED_GLOWSTONE = forgeTag("tools/shields/refined_glowstone");
        public static final TagKey<Item> TOOLS_SHIELDS_REFINED_OBSIDIAN = forgeTag("tools/shields/refined_obsidian");
        public static final TagKey<Item> TOOLS_SHIELDS_STEEL = forgeTag("tools/shields/steel");

        public static final TagKey<Item> ARMORS_HELMETS_BRONZE = forgeTag("armors/helmets/bronze");
        public static final TagKey<Item> ARMORS_HELMETS_LAPIS_LAZULI = forgeTag("armors/helmets/lapis_lazuli");
        public static final TagKey<Item> ARMORS_HELMETS_OSMIUM = forgeTag("armors/helmets/osmium");
        public static final TagKey<Item> ARMORS_HELMETS_REFINED_GLOWSTONE = forgeTag("armors/helmets/refined_glowstone");
        public static final TagKey<Item> ARMORS_HELMETS_REFINED_OBSIDIAN = forgeTag("armors/helmets/refined_obsidian");
        public static final TagKey<Item> ARMORS_HELMETS_STEEL = forgeTag("armors/helmets/steel");

        public static final TagKey<Item> ARMORS_CHESTPLATES_BRONZE = forgeTag("armors/chestplates/bronze");
        public static final TagKey<Item> ARMORS_CHESTPLATES_LAPIS_LAZULI = forgeTag("armors/chestplates/lapis_lazuli");
        public static final TagKey<Item> ARMORS_CHESTPLATES_OSMIUM = forgeTag("armors/chestplates/osmium");
        public static final TagKey<Item> ARMORS_CHESTPLATES_REFINED_GLOWSTONE = forgeTag("armors/chestplates/refined_glowstone");
        public static final TagKey<Item> ARMORS_CHESTPLATES_REFINED_OBSIDIAN = forgeTag("armors/chestplates/refined_obsidian");
        public static final TagKey<Item> ARMORS_CHESTPLATES_STEEL = forgeTag("armors/chestplates/steel");

        public static final TagKey<Item> ARMORS_LEGGINGS_BRONZE = forgeTag("armors/leggings/bronze");
        public static final TagKey<Item> ARMORS_LEGGINGS_LAPIS_LAZULI = forgeTag("armors/leggings/lapis_lazuli");
        public static final TagKey<Item> ARMORS_LEGGINGS_OSMIUM = forgeTag("armors/leggings/osmium");
        public static final TagKey<Item> ARMORS_LEGGINGS_REFINED_GLOWSTONE = forgeTag("armors/leggings/refined_glowstone");
        public static final TagKey<Item> ARMORS_LEGGINGS_REFINED_OBSIDIAN = forgeTag("armors/leggings/refined_obsidian");
        public static final TagKey<Item> ARMORS_LEGGINGS_STEEL = forgeTag("armors/leggings/steel");

        public static final TagKey<Item> ARMORS_BOOTS_BRONZE = forgeTag("armors/boots/bronze");
        public static final TagKey<Item> ARMORS_BOOTS_LAPIS_LAZULI = forgeTag("armors/boots/lapis_lazuli");
        public static final TagKey<Item> ARMORS_BOOTS_OSMIUM = forgeTag("armors/boots/osmium");
        public static final TagKey<Item> ARMORS_BOOTS_REFINED_GLOWSTONE = forgeTag("armors/boots/refined_glowstone");
        public static final TagKey<Item> ARMORS_BOOTS_REFINED_OBSIDIAN = forgeTag("armors/boots/refined_obsidian");
        public static final TagKey<Item> ARMORS_BOOTS_STEEL = forgeTag("armors/boots/steel");

        private static TagKey<Item> forgeTag(String name) {
            return ItemTags.create(new ResourceLocation("forge", name));
        }
    }
}