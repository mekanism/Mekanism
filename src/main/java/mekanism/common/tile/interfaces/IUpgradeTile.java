package mekanism.common.tile.interfaces;

import mekanism.api.Upgrade;
import mekanism.common.tile.component.TileComponentUpgrade;

public interface IUpgradeTile {

    default boolean supportsUpgrades() {
        return true;
    }

    default boolean supportsUpgrade(Upgrade upgradeType) {
        return supportsUpgrades() && getComponent().supports(upgradeType);
    }

    TileComponentUpgrade getComponent();

    void recalculateUpgrades(Upgrade upgradeType);
}