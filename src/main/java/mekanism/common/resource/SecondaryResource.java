package mekanism.common.resource;

import java.util.function.Supplier;
import mekanism.common.tags.MekanismTags;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;

public enum SecondaryResource implements IResource {
    URANIUM("uranium", 0x46664F, () -> MekanismTags.Items.ORES.get(OreType.URANIUM));

    private final int tint;
    private final String name;
    private final Supplier<Tag<Item>> oreTag;

    SecondaryResource(String name, int tint, Supplier<Tag<Item>> oreTag) {
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
        return oreTag.get();
    }
}