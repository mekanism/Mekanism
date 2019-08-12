package mekanism.common.tags;

import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

//TODO: Figure out if there is some more generic place these should be registered, rather than to our own modid
public class MekanismTags {

    public static class Blocks {

        /*public static final Tag<Block> CHESTS = tag("chests");
        public static final Tag<Block> CHESTS_ENDER = tag("chests/ender");
        public static final Tag<Block> CHESTS_TRAPPED = tag("chests/trapped");
        public static final Tag<Block> CHESTS_WOODEN = tag("chests/wooden");*/

        private static Tag<Block> tag(String name) {
            return new BlockTags.Wrapper(new ResourceLocation(Mekanism.MODID, name));
        }
    }

    public static class Items {

        /*
        OreDictionary.registerOre("alloy" + tier.getBaseTier().getSimpleName(), new ItemStack(this));
        if (tier == AlloyTier.ENRICHED) {
            OreDictionary.registerOre("itemEnrichedAlloy", new ItemStack(this));
        }
         */
        public static final Tag<Item> ALLOYS = tag("alloys");
        public static final Tag<Item> ALLOYS_BASIC = tag("alloys/basic");
        public static final Tag<Item> ALLOYS_ADVANCED = tag("alloys/advanced");
        public static final Tag<Item> ALLOYS_ELITE = tag("alloys/elite");
        public static final Tag<Item> ALLOYS_ULTIMATE = tag("alloys/ultimate");
        //TODO: What is the point of this one
        public static final Tag<Item> ALLOY_ENRICHED = tag("alloys/enriched");
        /*public static final Tag<Item> CHESTS = tag("chests");
        public static final Tag<Item> CHESTS_ENDER = tag("chests/ender");
        public static final Tag<Item> CHESTS_TRAPPED = tag("chests/trapped");
        public static final Tag<Item> CHESTS_WOODEN = tag("chests/wooden");*/

        private static Tag<Item> tag(String name) {
            return new ItemTags.Wrapper(new ResourceLocation(Mekanism.MODID, name));
        }
    }
}