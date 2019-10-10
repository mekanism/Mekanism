package mekanism.common.base;

import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.IOffsetCapability;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.security.ISecurityTile;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

//TODO: IC2
/*@InterfaceList({
      @Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = MekanismHooks.IC2_MOD_ID),
})*/
public interface IAdvancedBoundingBlock extends ICapabilityProvider, IBoundingBlock, IMekanismInventory, IStrictEnergyAcceptor, IStrictEnergyStorage, IComputerIntegration,
      ISpecialConfigData, ISecurityTile, IOffsetCapability {

    boolean canBoundReceiveEnergy(BlockPos location, Direction side);

    void onPower();

    void onNoPower();
}