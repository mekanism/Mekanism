package mekanism.common.block.attribute;

import mekanism.api.upgrade.Upgrade;
import mekanism.common.tags.MekanismTags;
import net.minecraft.tags.TagKey;

public record AttributeUpgradeSupport(TagKey<Upgrade> supportedUpgrades) implements Attribute {

    public static final AttributeUpgradeSupport SIMPLE_MACHINE_UPGRADES = new AttributeUpgradeSupport(MekanismTags.Upgrades.SIMPLE_MACHINE_UPGRADES);
    public static final AttributeUpgradeSupport DEFAULT_MACHINE_UPGRADES = new AttributeUpgradeSupport(MekanismTags.Upgrades.DEFAULT_MACHINE_UPGRADES);
    public static final AttributeUpgradeSupport DEFAULT_ADVANCED_MACHINE_UPGRADES = new AttributeUpgradeSupport(MekanismTags.Upgrades.DEFAULT_ADVANCED_MACHINE_UPGRADES);
    public static final AttributeUpgradeSupport MUFFLING_ONLY = new AttributeUpgradeSupport(MekanismTags.Upgrades.MUFFLING_ONLY);
    public static final AttributeUpgradeSupport ANCHOR_ONLY = new AttributeUpgradeSupport(MekanismTags.Upgrades.ANCHOR_ONLY);
    public static final AttributeUpgradeSupport QIO_FILTER_HANDLER = new AttributeUpgradeSupport(MekanismTags.Upgrades.QIO_FILTER_HANDLER);
}
