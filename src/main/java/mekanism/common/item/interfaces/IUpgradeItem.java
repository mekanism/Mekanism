package mekanism.common.item.interfaces;

import mekanism.api.Upgrade;

//TODO: Move this to the API package, and also replace it with a capability when replacing IGasItem with a capability
public interface IUpgradeItem {

    Upgrade getUpgradeType();
}