package mekanism.common.tileentity;

import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.block.IElectricalStorage;
import universalelectricity.core.electricity.ElectricityPack;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cofh.api.energy.IEnergyHandler;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import mekanism.api.Object3D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.IAdvancedBoundingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityAdvancedBoundingBlock extends TileEntityBoundingBlock implements ISidedInventory, IEnergySink, IStrictEnergyAcceptor, IPowerReceptor, IEnergyTile, IElectrical, IElectricalStorage, IConnector, IStrictEnergyStorage, IEnergyHandler
{
	@Override
	public int getSizeInventory() 
	{
		return getInv().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i) 
	{
		return getInv().getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) 
	{
		return getInv().decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) 
	{
		return getInv().getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) 
	{
		getInv().setInventorySlotContents(i, itemstack);
	}

	@Override
	public String getInvName() 
	{
		return getInv().getInvName();
	}

	@Override
	public boolean isInvNameLocalized() 
	{
		return getInv().isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return getInv().getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) 
	{
		return getInv().isUseableByPlayer(entityplayer);
	}

	@Override
	public void openChest() 
	{
		getInv().openChest();
	}

	@Override
	public void closeChest() 
	{
		getInv().closeChest();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) 
	{
		return getInv().isItemValidForSlot(i, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int slotID)
	{
		return getInv().getBoundSlots(Object3D.get(this), slotID);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return getInv().canInsertItem(i, itemstack, j);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) 
	{
		return getInv().canExtractItem(i, itemstack, j);
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) 
	{
		return getInv().acceptsEnergyFrom(emitter, direction);
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		return getInv().receiveEnergy(from, maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return getInv().extractEnergy(from, maxExtract, simulate);
	}

	@Override
	public boolean canInterface(ForgeDirection from) 
	{
		return getInv().canInterface(from);
	}

	@Override
	public int getEnergyStored(ForgeDirection from) 
	{
		return getInv().getEnergyStored(from);
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) 
	{
		return getInv().getMaxEnergyStored(from);
	}

	@Override
	public double getEnergy() 
	{
		return getInv().getEnergy();
	}

	@Override
	public void setEnergy(double energy) 
	{
		getInv().setEnergy(energy);
	}

	@Override
	public double getMaxEnergy() 
	{
		return getInv().getMaxEnergy();
	}

	@Override
	public boolean canConnect(ForgeDirection direction) 
	{
		return getInv().canConnect(direction);
	}

	@Override
	public void setEnergyStored(float energy) 
	{
		getInv().setEnergyStored(energy);
	}

	@Override
	public float getEnergyStored() 
	{
		return getInv().getEnergyStored();
	}

	@Override
	public float getMaxEnergyStored() 
	{
		return getInv().getMaxEnergyStored();
	}

	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive) 
	{
		return getInv().receiveElectricity(from, receive, doReceive);
	}

	@Override
	public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide) 
	{
		return getInv().provideElectricity(from, request, doProvide);
	}

	@Override
	public float getRequest(ForgeDirection direction) 
	{
		return getInv().getRequest(direction);
	}

	@Override
	public float getProvide(ForgeDirection direction) 
	{
		return getInv().getProvide(direction);
	}

	@Override
	public float getVoltage() 
	{
		return getInv().getVoltage();
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) 
	{
		return getInv().getPowerReceiver(side);
	}

	@Override
	public void doWork(PowerHandler workProvider) 
	{
		getInv().doWork(workProvider);
	}

	@Override
	public World getWorld() 
	{
		return getInv().getWorld();
	}

	@Override
	public double transferEnergyToAcceptor(double amount) 
	{
		return getInv().transferEnergyToAcceptor(amount);
	}

	@Override
	public boolean canReceiveEnergy(ForgeDirection side) 
	{
		return getInv().canReceiveEnergy(side);
	}

	@Override
	public double demandedEnergyUnits() 
	{
		return getInv().demandedEnergyUnits();
	}

	@Override
	public double injectEnergyUnits(ForgeDirection directionFrom, double amount) 
	{
		return getInv().injectEnergyUnits(directionFrom, amount);
	}

	@Override
	public int getMaxSafeInput() 
	{
		return getInv().getMaxSafeInput();
	}
	
	public IAdvancedBoundingBlock getInv()
	{
		return (IAdvancedBoundingBlock)new Object3D(mainX, mainY, mainZ).getTileEntity(worldObj);
	}
}
