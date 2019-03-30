package mekanism.common.base;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.tile.IEnergySink;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.capabilities.IOffsetCapability;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.security.ISecurityTile;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;

@InterfaceList({
      @Interface(iface = "cofh.redstoneflux.api.IEnergyProvider", modid = MekanismHooks.REDSTONEFLUX_MOD_ID),
      @Interface(iface = "cofh.redstoneflux.api.IEnergyReceiver", modid = MekanismHooks.REDSTONEFLUX_MOD_ID),
      @Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = MekanismHooks.IC2_MOD_ID),
})
public interface IAdvancedBoundingBlock extends ICapabilityProvider, IBoundingBlock, ISidedInventory, IEnergySink,
      IStrictEnergyAcceptor, IStrictEnergyStorage, IEnergyReceiver, IEnergyProvider, IComputerIntegration,
      ISpecialConfigData, ISecurityTile, IOffsetCapability {

    int[] getBoundSlots(BlockPos location, EnumFacing side);

    boolean canBoundInsert(BlockPos location, int i, ItemStack itemstack);

    boolean canBoundExtract(BlockPos location, int i, ItemStack itemstack, EnumFacing side);

    boolean canBoundReceiveEnergy(BlockPos location, EnumFacing side);

    void onPower();

    void onNoPower();
}
