package mekanism.common.resource;

import java.util.function.Supplier;
import mekanism.common.tags.MekanismTags;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

public enum PrimaryResource implements IResource {
    IRON("iron", 0xFFAF8E77, () -> Tags.Items.ORES_IRON, true, null, true),
    GOLD("gold", 0xFFF2CD67, () -> Tags.Items.ORES_GOLD, true, null, true),
    OSMIUM("osmium", 0xFF1E79C3, () -> MekanismTags.Items.ORES.get(OreType.OSMIUM), false, BlockResourceInfo.OSMIUM, true),
    COPPER("copper", 0xFFAA4B19, () -> MekanismTags.Items.ORES.get(OreType.COPPER), false, BlockResourceInfo.COPPER, true),
    TIN("tin", 0xFFCCCCD9, () -> MekanismTags.Items.ORES.get(OreType.TIN), false, BlockResourceInfo.TIN, true),
    LEAD("lead", 0xFF3A404A, () -> MekanismTags.Items.ORES.get(OreType.LEAD), false, BlockResourceInfo.LEAD, false),
    URANIUM("uranium", 0xFF46664F, () -> MekanismTags.Items.ORES.get(OreType.URANIUM), false, BlockResourceInfo.URANIUM, false);

    private String name;
    private int tint;
    private Supplier<Tag<Item>> oreTag;
    private boolean isVanilla;
    private BlockResourceInfo resourceBlockInfo;
    private boolean textureOverride;

    private PrimaryResource(String name, int tint, Supplier<Tag<Item>> oreTag, boolean isVanilla, BlockResourceInfo resourceBlockInfo, boolean textureOverride) {
        this.name = name;
        this.tint = tint;
        this.oreTag = oreTag;
        this.isVanilla = isVanilla;
        this.resourceBlockInfo = resourceBlockInfo;
        this.textureOverride = textureOverride;
    }

    // TODO remove
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

    public Tag<Item> getOreTag() {
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

    public boolean hasTextureOverride() {
        return textureOverride;
    }
}
