package mekanism.common.resource;

import java.util.List;
import java.util.function.Supplier;
import mekanism.common.registries.MekanismTrimMaterials;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

public enum PrimaryResource implements IResource {
    IRON("iron", 0xFFAF8E77, TrimMaterials.IRON, Tags.Items.ORES_IRON),
    GOLD("gold", 0xFFF2CD67, TrimMaterials.GOLD, Tags.Items.ORES_GOLD),
    OSMIUM("osmium", 0xFF1E79C3, MekanismTrimMaterials.OSMIUM, () -> MekanismTags.BlockItems.ORES.get(OreType.OSMIUM).item(), BlockResourceInfo.OSMIUM, BlockResourceInfo.RAW_OSMIUM),
    COPPER("copper", 0xFFAA4B19, TrimMaterials.COPPER, Tags.Items.ORES_COPPER),
    TIN("tin", 0xFFCCCCD9, MekanismTrimMaterials.TIN, () -> MekanismTags.BlockItems.ORES.get(OreType.TIN).item(), BlockResourceInfo.TIN, BlockResourceInfo.RAW_TIN),
    LEAD("lead", 0xFF3A404A, MekanismTrimMaterials.LEAD, () -> MekanismTags.BlockItems.ORES.get(OreType.LEAD).item(), BlockResourceInfo.LEAD, BlockResourceInfo.RAW_LEAD),
    URANIUM("uranium", 0xFF46664F, MekanismTrimMaterials.URANIUM, () -> MekanismTags.BlockItems.ORES.get(OreType.URANIUM).item(), BlockResourceInfo.URANIUM, BlockResourceInfo.RAW_URANIUM);

    /// Cached value of [PrimaryResource#values()].
    @Unmodifiable
    public static final List<PrimaryResource> VALUES = List.of(values());

    private final String name;
    private final int tint;
    //Note: This is a supplier because of the chicken and egg of referencing OreType and OreType referencing PrimaryResource
    private final Supplier<TagKey<Item>> oreTag;
    private final boolean isVanilla;
    @Nullable
    private final BlockResourceInfo resourceBlockInfo;
    @Nullable
    private final BlockResourceInfo rawResourceBlockInfo;
    private final ResourceKey<TrimMaterial> trimMaterial;

    PrimaryResource(String name, int tint, ResourceKey<TrimMaterial> trimMaterial, TagKey<Item> oreTag) {
        this(name, tint, trimMaterial, () -> oreTag, true, null, null);
    }

    PrimaryResource(String name, int tint, ResourceKey<TrimMaterial> trimMaterial, Supplier<TagKey<Item>> oreTag, @Nullable BlockResourceInfo resourceBlockInfo, @Nullable BlockResourceInfo rawResourceBlockInfo) {
        this(name, tint, trimMaterial, oreTag, false, resourceBlockInfo, rawResourceBlockInfo);
    }

    PrimaryResource(String name, int tint, ResourceKey<TrimMaterial> trimMaterial, Supplier<TagKey<Item>> oreTag, boolean isVanilla, @Nullable BlockResourceInfo resourceBlockInfo, @Nullable BlockResourceInfo rawResourceBlockInfo) {
        this.name = name;
        this.tint = tint;
        this.oreTag = oreTag;
        this.isVanilla = isVanilla;
        this.resourceBlockInfo = resourceBlockInfo;
        this.rawResourceBlockInfo = rawResourceBlockInfo;
        this.trimMaterial = trimMaterial;
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

    @Override
    public Item.Properties modifyProperties(Item.Properties properties, @Nullable ResourceType resourceType) {
        if (resourceType == ResourceType.INGOT) {
            properties = properties.trimMaterial(trimMaterial);
        }
        return properties;
    }
}
