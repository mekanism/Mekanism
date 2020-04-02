package mekanism.common;

import mekanism.common.resource.INamedResource;
import mekanism.common.tags.MekanismTags;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

public enum Resource implements INamedResource {
    IRON("iron", 0xAF8E77, Tags.Items.ORES_IRON),
    GOLD("gold", 0xF2CD67, Tags.Items.ORES_GOLD),
    OSMIUM("osmium", 0x1E79C3, MekanismTags.Items.ORES_OSMIUM),
    COPPER("copper", 0xAA4B19, MekanismTags.Items.ORES_COPPER),
    TIN("tin", 0xCCCCD9, MekanismTags.Items.ORES_TIN),
    FLUORITE("fluorite", 0xC6C7BD, MekanismTags.Items.ORES_FLUORITE),
    URANIUM("uranium", 0x46664F, MekanismTags.Items.ORES_URANIUM);

    private final int tint;
    private final String name;
    private final Tag<Item> oreTag;

    Resource(String name, int tint, Tag<Item> oreTag) {
        this.name = name;
        this.tint = tint;
        this.oreTag = oreTag;
    }

    @Override
    public String getRegistrySuffix() {
        return name;
    }

    public int getTint() {
        return tint;
    }

    public Tag<Item> getOreTag() {
        return oreTag;
    }
}