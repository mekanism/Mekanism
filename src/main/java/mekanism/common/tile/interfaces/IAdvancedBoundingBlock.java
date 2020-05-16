package mekanism.common.tile.interfaces;

import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.common.capabilities.IOffsetCapability;
import mekanism.common.lib.security.ISecurityTile;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IAdvancedBoundingBlock extends ICapabilityProvider, IBoundingBlock, ISecurityTile, ISpecialConfigData, IOffsetCapability {

    void onPower();

    void onNoPower();
}