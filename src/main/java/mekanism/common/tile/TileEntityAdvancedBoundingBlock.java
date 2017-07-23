package mekanism.common.tile;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import mekanism.api.Coord4D;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.base.IAdvancedBoundingBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
	@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
	@Interface(iface = "cofh.redstoneflux.api.IEnergyProvider", modid = "redstoneflux"),
	@Interface(iface = "cofh.redstoneflux.api.IEnergyReceiver", modid = "redstoneflux")
})
public class TileEntityAdvancedBoundingBlock extends TileEntityBoundingBlock implements ISidedInventory, IEnergySink, IStrictEnergyAcceptor, IEnergyReceiver, IEnergyProvider, IComputerIntegration, ISpecialConfigData
{
	@Override
	public boolean isEmpty()
	{
		if(getInv() == null)
		{
			return true;
		}
		
		return getInv().isEmpty();
	}
	
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
			return ItemStack.EMPTY;
		}

		return getInv().getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if(getInv() == null)
		{
			return ItemStack.EMPTY;
		}

		return getInv().decrStackSize(i, j);
	}

	@Override
	public ItemStack removeStackFromSlot(int i)
	{
		if(getInv() == null)
		{
			return ItemStack.EMPTY;
		}

		return getInv().removeStackFromSlot(i);
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
	public String getName()
	{
		if(getInv() == null)
		{
			return "null";
		}

		return getInv().getName();
	}

	@Override
	public boolean hasCustomName()
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().hasCustomName();
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(getName());
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
	public boolean isUsableByPlayer(EntityPlayer entityplayer)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().isUsableByPlayer(entityplayer);
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
		if(getInv() == null)
		{
			return;
		}

		getInv().openInventory(player);
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
		if(getInv() == null)
		{
			return;
		}

		getInv().closeInventory(player);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().canBoundInsert(getPos(), i, itemstack);
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{

	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear() {}

	@Override
	public int[] getSlotsForFace(EnumFacing side)
	{
		if(getInv() == null)
		{
			return InventoryUtils.EMPTY;
		}

		return getInv().getBoundSlots(getPos(), side);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing side)
	{
		return isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing side)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().canBoundExtract(getPos(), i, itemstack, side);
	}

	@Override
	@Method(modid = "IC2")
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().acceptsEnergyFrom(emitter, direction);
	}

	@Override
	@Method(modid = "redstoneflux")
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		if(getInv() == null || !canReceiveEnergy(from))
		{
			return 0;
		}

		return getInv().receiveEnergy(from, maxReceive, simulate);
	}

	@Override
	@Method(modid = "redstoneflux")
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().extractEnergy(from, maxExtract, simulate);
	}

	@Override
	@Method(modid = "redstoneflux")
	public boolean canConnectEnergy(EnumFacing from)
	{
		if(getInv() == null)
		{
			return false;
		}

		return canReceiveEnergy(from);
	}

	@Override
	@Method(modid = "redstoneflux")
	public int getEnergyStored(EnumFacing from)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getEnergyStored(from);
	}

	@Override
	@Method(modid = "redstoneflux")
	public int getMaxEnergyStored(EnumFacing from)
	{
		if(getInv() == null)
		{
			return 0;
		}

		return getInv().getMaxEnergyStored(from);
	}

	@Override
	public double acceptEnergy(EnumFacing side, double amount, boolean simulate)
	{
		if(getInv() == null || !canReceiveEnergy(side))
		{
			return 0;
		}

		return getInv().acceptEnergy(side, amount, simulate);
	}

	@Override
	public boolean canReceiveEnergy(EnumFacing side)
	{
		if(getInv() == null)
		{
			return false;
		}

		return getInv().canBoundReceiveEnergy(getPos(), side);
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
		if(getInv() == null || !canReceiveEnergy(directionFrom))
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
		
		TileEntity tile = new Coord4D(mainPos, world).getTileEntity(world);

		if(!(tile instanceof IAdvancedBoundingBlock))
		{
			world.setBlockToAir(mainPos);
			return null;
		}

		return (IAdvancedBoundingBlock)new Coord4D(mainPos, world).getTileEntity(world);
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
	public NBTTagCompound getConfigurationData(NBTTagCompound nbtTags)
	{
		if(getInv() == null)
		{
			return new NBTTagCompound();
		}
		
		return getInv().getConfigurationData(nbtTags);
	}

	@Override
	public void setConfigurationData(NBTTagCompound nbtTags)
	{
		if(getInv() == null)
		{
			return;
		}
		
		getInv().setConfigurationData(nbtTags);
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

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(getInv() == null || capability == Capabilities.TILE_NETWORK_CAPABILITY)
		{
			return super.hasCapability(capability, facing);
		}
		
		return getInv().hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(getInv() == null || capability == Capabilities.TILE_NETWORK_CAPABILITY)
		{
			return super.getCapability(capability, facing);
		}
		
		return getInv().getCapability(capability, facing);
	}
}
