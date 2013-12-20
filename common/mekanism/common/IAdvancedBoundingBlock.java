package mekanism.common;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.block.IElectricalStorage;
import buildcraft.api.power.IPowerReceptor;
import cofh.api.energy.IEnergyHandler;
import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public interface IAdvancedBoundingBlock extends IBoundingBlock, ISidedInventory, IEnergySink, IStrictEnergyAcceptor, IPowerReceptor, IEnergyTile, IElectrical, IElectricalStorage, IConnector, IStrictEnergyStorage, IEnergyHandler
{
	public int[] getBoundSlots(Coord4D location, int side);
	
	public boolean canBoundInsert(Coord4D location, int i, ItemStack itemstack);

	public boolean canBoundExtract(Coord4D location, int i, ItemStack itemstack, int j);
}
