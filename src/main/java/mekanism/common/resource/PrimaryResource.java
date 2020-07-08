package mekanism.common.resource;

import java.util.function.Supplier;
import mekanism.common.tags.MekanismTags;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

public enum PrimaryResource implements IResource {
    IRON("iron", 0xFFAF8E77, Tags.Items.ORES_IRON),
    GOLD("gold", 0xFFF2CD67, Tags.Items.ORES_GOLD),
    OSMIUM("osmium", 0xFF1E79C3, () -> MekanismTags.Items.ORES.get(OreType.OSMIUM), BlockResourceInfo.OSMIUM),
    COPPER("copper", 0xFFAA4B19, () -> MekanismTags.Items.ORES.get(OreType.COPPER), BlockResourceInfo.COPPER),
    TIN("tin", 0xFFCCCCD9, () -> MekanismTags.Items.ORES.get(OreType.TIN), BlockResourceInfo.TIN),
    LEAD("lead", 0xFF3A404A, () -> MekanismTags.Items.ORES.get(OreType.LEAD), BlockResourceInfo.LEAD),
    URANIUM("uranium", 0xFF46664F, () -> MekanismTags.Items.ORES.get(OreType.URANIUM), BlockResourceInfo.URANIUM);

    private final String name;
    private final int tint;
    private final Supplier<ITag<Item>> oreTag;
    private final boolean isVanilla;
    private final BlockResourceInfo resourceBlockInfo;

    PrimaryResource(String name, int tint, ITag<Item> oreTag) {
        this(name, tint, () -> oreTag, true, null);
    }

    PrimaryResource(String name, int tint, Supplier<ITag<Item>> oreTag, BlockResourceInfo resourceBlockInfo) {
        this(name, tint, oreTag, false, resourceBlockInfo);
    }

    PrimaryResource(String name, int tint, Supplier<ITag<Item>> oreTag, boolean isVanilla, BlockResourceInfo resourceBlockInfo) {
        this.name = name;
        this.tint = tint;
        this.oreTag = oreTag;
        this.isVanilla = isVanilla;
        this.resourceBlockInfo = resourceBlockInfo;
    }

    //TODO: remove
    public String getName() {
        return name;
    }

    @Override
    public String getRegistrySuffix() {
        return name;
    }

    public int getTint() {
        return tint;
    }

    public ITag<Item> getOreTag() {
        return oreTag.get();
    }

    public boolean has(ResourceType type) {
        return type != ResourceType.ENRICHED && (!isVanilla || type != ResourceType.INGOT && type != ResourceType.NUGGET);
    }

    public boolean isVanilla() {
        return isVanilla;
    }

    public BlockResourceInfo getResourceBlockInfo() {
        return resourceBlockInfo;
    }
}
