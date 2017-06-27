package mekanism.common.base;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.tile.IEnergySink;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
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
	@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
})
public interface IAdvancedBoundingBlock extends ICapabilityProvider, IBoundingBlock, ISidedInventory, IEnergySink, IStrictEnergyAcceptor, IStrictEnergyStorage, IEnergyReceiver, IEnergyProvider, IComputerIntegration, ISpecialConfigData, ISecurityTile
{
	public int[] getBoundSlots(BlockPos location, EnumFacing side);

	public boolean canBoundInsert(BlockPos location, int i, ItemStack itemstack);

	public boolean canBoundExtract(BlockPos location, int i, ItemStack itemstack, EnumFacing side);
	
	public boolean canBoundReceiveEnergy(BlockPos location, EnumFacing side);

	public void onPower();

	public void onNoPower();
}
