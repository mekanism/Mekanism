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
import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.IAdvancedBoundingBlock;
import mekanism.common.util.InventoryUtils;
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
	public String getInvName() 
	{
		if(getInv() == null)
		{
			return null;
		}
		
		return getInv().getInvName();
	}

	@Override
	public boolean isInvNameLocalized() 
	{
		if(getInv() == null)
		{
			return false;
		}
		
		return getInv().isInvNameLocalized();
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
	public void openChest() 
	{
		if(getInv() == null)
		{
			return;
		}
		
		getInv().openChest();
	}

	@Override
	public void closeChest() 
	{
		if(getInv() == null)
		{
			return;
		}
		
		getInv().closeChest();
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
	public boolean canConnect(ForgeDirection direction) 
	{
		if(getInv() == null)
		{
			return false;
		}
		
		return getInv().canConnect(direction);
	}

	@Override
	public void setEnergyStored(float energy) 
	{
		if(getInv() == null)
		{
			return;
		}
		
		getInv().setEnergyStored(energy);
	}

	@Override
	public float getEnergyStored() 
	{
		if(getInv() == null)
		{
			return 0;
		}
		
		return getInv().getEnergyStored();
	}

	@Override
	public float getMaxEnergyStored() 
	{
		if(getInv() == null)
		{
			return 0;
		}
		
		return getInv().getMaxEnergyStored();
	}

	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive) 
	{
		if(getInv() == null)
		{
			return 0;
		}
		
		return getInv().receiveElectricity(from, receive, doReceive);
	}

	@Override
	public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide) 
	{
		if(getInv() == null)
		{
			return null;
		}
		
		return getInv().provideElectricity(from, request, doProvide);
	}

	@Override
	public float getRequest(ForgeDirection direction) 
	{
		if(getInv() == null)
		{
			return 0;
		}
		
		return getInv().getRequest(direction);
	}

	@Override
	public float getProvide(ForgeDirection direction) 
	{
		if(getInv() == null)
		{
			return 0;
		}
		
		return getInv().getProvide(direction);
	}

	@Override
	public float getVoltage() 
	{
		if(getInv() == null)
		{
			return 0;
		}
		
		return getInv().getVoltage();
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
			return amount;
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
		return (IAdvancedBoundingBlock)new Coord4D(mainX, mainY, mainZ).getTileEntity(worldObj);
	}
}
