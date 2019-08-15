package mekanism.common;

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

//TODO: Put Tag Wrappers used throughout in this class
public class MekanismTags {

    public static final Tag<Item> GOLD_DUST = new ItemTags.Wrapper(new ResourceLocation("forge", "dusts/gold"));
}