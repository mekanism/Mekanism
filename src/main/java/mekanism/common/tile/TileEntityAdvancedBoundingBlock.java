package mekanism.common.tile;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import mekanism.api.Coord4D;
import mekanism.api.IFilterAccess;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.IAdvancedBoundingBlock;
import mekanism.common.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cofh.api.energy.IEnergyHandler;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;

public class TileEntityAdvancedBoundingBlock extends TileEntityBoundingBlock implements ISidedInventory, IEnergySink, IStrictEnergyAcceptor, IPowerReceptor, IEnergyTile, IStrictEnergyStorage, IEnergyHandler, IPeripheral, IFilterAccess
{
	@Override
	public int getSizeInventory()
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		if(getInv() == null)
		{
			return null;
		}

		return getInv().getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if(getInv() == null)
		{
			return null;
		}

		return getInv().decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		if(getInv() == null)
		{
			return null;
		}

		return getInv().getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		if(getInv() == null)
		{
			return;
		}

		getInv().setInventorySlotContents(i, itemstack);
	}

	@Override
	public String getInventoryName()
	{
		if(getInv() == null)
		{
			return null;
		}

		return getInv().getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().isUseableByPlayer(entityplayer);
	}

	@Override
	public void openInventory()
	{
		if(getInv() == null)
		{
			return;
		}

		getInv().openInventory();
	}

	@Override
	public void closeInventory()
	{
		if(getInv() == null)
		{
			return;
		}

		getInv().closeInventory();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().canBoundInsert(Coord4D.get(this), i, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int slotID)
	{
		if(getInv() == null)
		{
			return InventoryUtils.EMPTY;
		}

		return getInv().getBoundSlots(Coord4D.get(this), slotID);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().canBoundExtract(Coord4D.get(this), i, itemstack, j);
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().acceptsEnergyFrom(emitter, direction);
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().receiveEnergy(from, maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().extractEnergy(from, maxExtract, simulate);
	}

	@Override
	public boolean canInterface(ForgeDirection from)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().canInterface(from);
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getEnergyStored(from);
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getMaxEnergyStored(from);
	}

	@Override
	public double getEnergy()
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getEnergy();
	}

	@Override
	public void setEnergy(double energy)
	{
		if(getInv() == null)
		{
			return;
		}

		getInv().setEnergy(energy);
	}

	@Override
	public double getMaxEnergy()
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getMaxEnergy();
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		if(getInv() == null)
		{
			return null;
		}

		return getInv().getPowerReceiver(side);
	}

	@Override
	public void doWork(PowerHandler workProvider)
	{
		if(getInv() == null)
		{
			return;
		}

		getInv().doWork(workProvider);
	}

	@Override
	public World getWorld()
	{
		if(getInv() == null)
		{
			return null;
		}

		return getInv().getWorld();
	}

	@Override
	public double transferEnergyToAcceptor(ForgeDirection side, double amount)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().transferEnergyToAcceptor(side, amount);
	}

	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().canReceiveEnergy(side);
	}

	@Override
	public double demandedEnergyUnits()
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().demandedEnergyUnits();
	}

	@Override
	public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
	{
		if(getInv() == null)
		{
			return amount;
		}

		return getInv().injectEnergyUnits(directionFrom, amount);
	}

	@Override
	public int getMaxSafeInput()
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getMaxSafeInput();
	}

	public IAdvancedBoundingBlock getInv()
	{
		TileEntity tile = new Coord4D(mainX, mainY, mainZ, worldObj.provider.dimensionId).getTileEntity(worldObj);

		if(!(tile instanceof IAdvancedBoundingBlock))
		{
			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			return null;
		}

		return (IAdvancedBoundingBlock)new Coord4D(mainX, mainY, mainZ, worldObj.provider.dimensionId).getTileEntity(worldObj);
	}

	@Override
	public void onPower()
	{
		super.onPower();

		if(getInv() != null)
		{
			getInv().onPower();
		}
	}

	@Override
	public void onNoPower()
	{
		super.onNoPower();

		if(getInv() != null)
		{
			getInv().onNoPower();
		}
	}

	@Override
	public String getType()
	{
		if(getInv() == null)
		{
			return null;
		}

		return getInv().getType();
	}

	@Override
	public String[] getMethodNames()
	{
		if(getInv() == null)
		{
			return null;
		}

		return getInv().getMethodNames();
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		if(getInv() == null)
		{
			return null;
		}

		return getInv().callMethod(computer, context, method, arguments);
	}

	@Override
	public void attach(IComputerAccess computer)
	{
		if(getInv() == null)
		{
			return;
		}

		getInv().attach(computer);
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		if(getInv() == null)
		{
			return;
		}

		getInv().detach(computer);
	}

	@Override
	public boolean equals(IPeripheral other)
	{
		return this == other;
	}

	@Override
	public NBTTagCompound getFilterData(NBTTagCompound nbtTags)
	{
		if(getInv() == null)
		{
			return null;
		}
		
		return getInv().getFilterData(nbtTags);
	}

	@Override
	public void setFilterData(NBTTagCompound nbtTags)
	{
		if(getInv() == null)
		{
			return;
		}
		
		getInv().setFilterData(nbtTags);
	}

	@Override
	public String getDataType()
	{
		if(getInv() == null)
		{
			return "null";
		}
		
		return getInv().getDataType();
	}
}
