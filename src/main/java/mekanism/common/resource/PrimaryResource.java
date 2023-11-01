package mekanism.common.resource;

import java.util.function.Supplier;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

public enum PrimaryResource implements IResource {
    IRON("iron", 0xFFAF8E77, Tags.Items.ORES_IRON),
    GOLD("gold", 0xFFF2CD67, Tags.Items.ORES_GOLD),
    OSMIUM("osmium", 0xFF1E79C3, () -> MekanismTags.Items.ORES.get(OreType.OSMIUM), BlockResourceInfo.OSMIUM, BlockResourceInfo.RAW_OSMIUM),
    COPPER("copper", 0xFFAA4B19, Tags.Items.ORES_COPPER),
    TIN("tin", 0xFFCCCCD9, () -> MekanismTags.Items.ORES.get(OreType.TIN), BlockResourceInfo.TIN, BlockResourceInfo.RAW_TIN),
    LEAD("lead", 0xFF3A404A, () -> MekanismTags.Items.ORES.get(OreType.LEAD), BlockResourceInfo.LEAD, BlockResourceInfo.RAW_LEAD),
    URANIUM("uranium", 0xFF46664F, () -> MekanismTags.Items.ORES.get(OreType.URANIUM), BlockResourceInfo.URANIUM, BlockResourceInfo.RAW_URANIUM);

    private final String name;
    private final int tint;
    //Note: This is a supplier because of the chicken and egg of referencing OreType and OreType referencing PrimaryResource
    private final Supplier<TagKey<Item>> oreTag;
    private final boolean isVanilla;
    private final BlockResourceInfo resourceBlockInfo;
    private final BlockResourceInfo rawResourceBlockInfo;

    PrimaryResource(String name, int tint, TagKey<Item> oreTag) {
        this(name, tint, () -> oreTag, true, null, null);
    }

    PrimaryResource(String name, int tint, Supplier<TagKey<Item>> oreTag, BlockResourceInfo resourceBlockInfo, BlockResourceInfo rawResourceBlockInfo) {
        this(name, tint, oreTag, false, resourceBlockInfo, rawResourceBlockInfo);
    }

    PrimaryResource(String name, int tint, Supplier<TagKey<Item>> oreTag, boolean isVanilla, BlockResourceInfo resourceBlockInfo, BlockResourceInfo rawResourceBlockInfo) {
        this.name = name;
        this.tint = tint;
        this.oreTag = oreTag;
        this.isVanilla = isVanilla;
        this.resourceBlockInfo = resourceBlockInfo;
        this.rawResourceBlockInfo = rawResourceBlockInfo;
    }

    @Override
    public String getRegistrySuffix() {
        return name;
    }

    public int getTint() {
        return tint;
    }

    public TagKey<Item> getOreTag() {
        return oreTag.get();
    }

    public boolean has(ResourceType type) {
        return type != ResourceType.ENRICHED && (!isVanilla || !type.isVanilla());
    }

    public boolean isVanilla() {
        return isVanilla;
    }

    @Nullable
    public BlockResourceInfo getResourceBlockInfo() {
        return resourceBlockInfo;
    }

    @Nullable
    public BlockResourceInfo getRawResourceBlockInfo() {
        return rawResourceBlockInfo;
    }
}
