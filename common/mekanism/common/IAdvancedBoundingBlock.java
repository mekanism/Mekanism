package mekanism.common;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import buildcraft.api.power.IPowerReceptor;
import cofh.api.energy.IEnergyHandler;
import dan200.computer.api.IPeripheral;

public interface IAdvancedBoundingBlock extends IBoundingBlock, ISidedInventory, IEnergySink, IStrictEnergyAcceptor, IPowerReceptor, IEnergyTile, IStrictEnergyStorage, IEnergyHandler, IPeripheral
{
	public int[] getBoundSlots(Coord4D location, int side);
	
	public boolean canBoundInsert(Coord4D location, int i, ItemStack itemstack);

	public boolean canBoundExtract(Coord4D location, int i, ItemStack itemstack, int j);
	
	public void onPower();
	
	public void onNoPower();
}
