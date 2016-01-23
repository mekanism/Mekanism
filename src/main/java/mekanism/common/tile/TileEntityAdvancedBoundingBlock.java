package mekanism.common.tile;

import cofh.api.energy.IEnergyHandler;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import ic2.api.energy.tile.IEnergySink;
import mekanism.api.Coord4D;
import mekanism.api.IFilterAccess;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.base.IAdvancedBoundingBlock;
import mekanism.common.integration.IComputerIntegration;
import mekanism.common.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

@InterfaceList({
		@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
		@Interface(iface = "cofh.api.energy.IEnergyHandler", modid = "CoFHCore")
})
public class TileEntityAdvancedBoundingBlock extends TileEntityBoundingBlock implements ISidedInventory, IEnergySink, IStrictEnergyAcceptor, IEnergyHandler, IComputerIntegration, IFilterAccess
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
			return "null";
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
	public int[] getSlotsForFace(int slotID)
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
	@Method(modid = "IC2")
	public boolean acceptsEnergyFrom(TileEntity emitter, EnumFacing direction)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().acceptsEnergyFrom(emitter, direction);
	}

	@Override
	@Method(modid = "CoFHCore")
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().receiveEnergy(from, maxReceive, simulate);
	}

	@Override
	@Method(modid = "CoFHCore")
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().extractEnergy(from, maxExtract, simulate);
	}

	@Override
	@Method(modid = "CoFHCore")
	public boolean canConnectEnergy(EnumFacing from)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().canConnectEnergy(from);
	}

	@Override
	@Method(modid = "CoFHCore")
	public int getEnergyStored(EnumFacing from)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getEnergyStored(from);
	}

	@Override
	@Method(modid = "CoFHCore")
	public int getMaxEnergyStored(EnumFacing from)
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
	public double transferEnergyToAcceptor(EnumFacing side, double amount)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().transferEnergyToAcceptor(side, amount);
	}

	@Override
	public boolean canReceiveEnergy(EnumFacing side)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().canReceiveEnergy(side);
	}

	@Override
	@Method(modid = "IC2")
	public double getDemandedEnergy()
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getDemandedEnergy();
	}

	@Override
	@Method(modid = "IC2")
	public double injectEnergy(EnumFacing directionFrom, double amount, double voltage)
	{
		if(getInv() == null)
		{
			return amount;
		}

		return getInv().injectEnergy(directionFrom, amount, voltage);
	}

	@Override
	@Method(modid = "IC2")
	public int getSinkTier()
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getSinkTier();
	}

	public IAdvancedBoundingBlock getInv()
	{
		if(!receivedCoords)
		{
			return null;
		}
		
		TileEntity tile = new Coord4D(mainX, mainY, mainZ, worldObj.provider.getDimensionId()).getTileEntity(worldObj);

		if(!(tile instanceof IAdvancedBoundingBlock))
		{
			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			return null;
		}

		return (IAdvancedBoundingBlock)new Coord4D(mainX, mainY, mainZ, worldObj.provider.getDimensionId()).getTileEntity(worldObj);
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
	public String[] getMethods()
	{
		if(getInv() == null)
		{
			return new String[] {};
		}

		return getInv().getMethods();
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws Exception
	{
		if(getInv() == null)
		{
			return new Object[] {};
		}

		return getInv().invoke(method, arguments);
	}

	@Override
	public NBTTagCompound getFilterData(NBTTagCompound nbtTags)
	{
		if(getInv() == null)
		{
			return new NBTTagCompound();
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
