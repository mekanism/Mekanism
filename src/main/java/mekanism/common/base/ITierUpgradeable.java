package mekanism.common.base;

import mekanism.common.tier.BaseTier;

@Deprecated
public interface ITierUpgradeable {

    boolean upgrade(BaseTier upgradeTier);
}