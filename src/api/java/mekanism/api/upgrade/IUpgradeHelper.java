package mekanism.api.upgrade;

import mekanism.api.IDynamicItemHelper;
import mekanism.api.MekanismAPI;

/// Helper class for interacting with upgrades.
///
/// @see IUpgradeHelper#INSTANCE
/// @since 10.8.0
public interface IUpgradeHelper extends IDynamicItemHelper<Upgrade> {

    /// Provides access to Mekanism's implementation of [IUpgradeHelper].
    IUpgradeHelper INSTANCE = MekanismAPI.getService(IUpgradeHelper.class);
}