package mekanism.common.tile.interfaces;

import mekanism.api.Upgrade;
import mekanism.common.tile.component.TileComponentUpgrade;
import org.jspecify.annotations.Nullable;

public interface IUpgradeTile {

    default boolean supportsUpgrades() {
        return true;
    }

    default boolean supportsUpgrade(Upgrade upgradeType) {
        if (supportsUpgrades()) {
            TileComponentUpgrade component = getComponent();
            //Note: This should never be null given supportsUpgrades is true, but if it is, handle it gracefully
            return component != null && component.supports(upgradeType);
        }
        return false;
    }

    default int getUpgrades(Upgrade upgradeType) {
        TileComponentUpgrade component = getComponent();
        return component == null ? 0 : component.getUpgrades(upgradeType);
    }

    default int addUpgrades(Upgrade upgrade, int maxAvailable) {
        TileComponentUpgrade component = getComponent();
        return component == null ? 0 : component.addUpgrades(upgrade, maxAvailable);
    }

    @Nullable
    TileComponentUpgrade getComponent();

    void recalculateUpgrades(Upgrade upgradeType);
}