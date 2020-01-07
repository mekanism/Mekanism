package mekanism.common.tile.interfaces;

import javax.annotation.Nullable;
import mekanism.common.upgrade.IUpgradeData;

//TODO: Come up with a better name for this interface
public interface IUpgradeableTile {

    boolean canBeUpgraded();

    /**
     * @return The upgrade data for this block or null if something went wrong
     */
    @Nullable
    default IUpgradeData getUpgradeData() {
        return null;
    }
}