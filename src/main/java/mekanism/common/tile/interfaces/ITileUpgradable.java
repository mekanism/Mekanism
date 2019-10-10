package mekanism.common.tile.interfaces;

import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.common.base.IUpgradeTile;

public interface ITileUpgradable extends IUpgradeTile {

    default boolean supportsUpgrades() {
        return true;
    }

    Set<Upgrade> getSupportedUpgrade();
}