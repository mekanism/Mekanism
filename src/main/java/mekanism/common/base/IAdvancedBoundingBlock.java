package mekanism.common.base;

import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.IFilterAccess;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;

import cofh.api.energy.IEnergyHandler;
import dan200.computercraft.api.peripheral.IPeripheral;
import ic2.api.energy.tile.IEnergySink;

@InterfaceList({
		@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
		@Interface(iface = "cofh.api.energy.IEnergyHandler", modid = "CoFHAPI|energy"),
		@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
})
public interface IAdvancedBoundingBlock extends IBoundingBlock, ISidedInventory, IEnergySink, IStrictEnergyAcceptor, IStrictEnergyStorage, IEnergyHandler, IPeripheral, IFilterAccess, IConfigurable
{
	public int[] getBoundSlots(Coord4D location, EnumFacing side);

	public boolean canBoundInsert(Coord4D location, int index, ItemStack itemstack);

	public boolean canBoundExtract(Coord4D location, int index, ItemStack itemstack, EnumFacing side);

	public void onPower();

	public void onNoPower();
}
