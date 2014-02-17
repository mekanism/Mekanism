package mekanism.common;

import mekanism.common.tile.component.TileComponentUpgrade;

public interface IUpgradeTile extends IUpgradeManagement
{
	public TileComponentUpgrade getComponent();
}
