package mekanism.common.block.attribute;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import mekanism.api.Upgrade;
import org.jetbrains.annotations.NotNull;

public record AttributeUpgradeSupport(@NotNull Set<Upgrade> supportedUpgrades) implements Attribute {

    public static final AttributeUpgradeSupport DEFAULT_MACHINE_UPGRADES = create(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);
    public static final AttributeUpgradeSupport DEFAULT_ADVANCED_MACHINE_UPGRADES = create(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.CHEMICAL);
    public static final AttributeUpgradeSupport SPEED_ENERGY = create(Upgrade.SPEED, Upgrade.ENERGY);
    public static final AttributeUpgradeSupport MUFFLING_ONLY = create(Upgrade.MUFFLING);
    public static final AttributeUpgradeSupport ENERGY_ONLY = create(Upgrade.ENERGY);
    public static final AttributeUpgradeSupport SPEED_ONLY = create(Upgrade.SPEED);
    public static final AttributeUpgradeSupport ANCHOR_ONLY = create(Upgrade.ANCHOR);

    public static AttributeUpgradeSupport create(Upgrade... supportedUpgrades) {
        if (supportedUpgrades.length == 0) {
            throw new IllegalArgumentException("There must be at least one upgrade that is supported");
        }
        Set<Upgrade> upgrades;
        if (supportedUpgrades.length == 1) {
            upgrades = Set.of(supportedUpgrades[0]);
        } else if (supportedUpgrades.length == 2) {
            upgrades = Set.of(supportedUpgrades[0], supportedUpgrades[1]);
        } else {
            upgrades = EnumSet.noneOf(Upgrade.class);
            Collections.addAll(upgrades, supportedUpgrades);
            upgrades = Collections.unmodifiableSet(upgrades);
        }
        return new AttributeUpgradeSupport(upgrades);
    }
}
