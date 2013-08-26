package mekanism.common;

import mekanism.api.IUpgradeManagement;

public interface IUpgradeTile extends IUpgradeManagement
{
	public TileComponentUpgrade getComponent();
}
