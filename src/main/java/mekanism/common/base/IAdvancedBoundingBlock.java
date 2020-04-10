package mekanism.common.base;

import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.common.capabilities.IOffsetCapability;
import mekanism.common.security.ISecurityTile;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IAdvancedBoundingBlock extends ICapabilityProvider, IBoundingBlock, ISecurityTile, ISpecialConfigData, IOffsetCapability {

    void onPower();

    void onNoPower();
}