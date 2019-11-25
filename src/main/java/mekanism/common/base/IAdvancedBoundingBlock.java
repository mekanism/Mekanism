package mekanism.common.base;

import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.capabilities.IOffsetCapability;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.security.ISecurityTile;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IAdvancedBoundingBlock extends ICapabilityProvider, IBoundingBlock, IStrictEnergyAcceptor, IStrictEnergyStorage, IComputerIntegration, ISecurityTile,
      ISpecialConfigData, IOffsetCapability {

    boolean canBoundReceiveEnergy(BlockPos location, Direction side);

    void onPower();

    void onNoPower();
}