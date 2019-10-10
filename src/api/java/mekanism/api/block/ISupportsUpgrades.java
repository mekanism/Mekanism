package mekanism.api.block;

import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;

//TODO: Rename to IUpgradeHolder??
public interface ISupportsUpgrades {

    //TODO: Move list of supported upgrades to here from having to be in TileEntity

    //TODO: Double check all machines/blocks do implement this that are supposed to

    @Nonnull
    Set<Upgrade> getSupportedUpgrade();
}