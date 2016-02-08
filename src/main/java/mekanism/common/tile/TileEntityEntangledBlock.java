package mekanism.common.tile;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.PacketHandler;
import mekanism.common.content.teleportation.SharedInventory;
import mekanism.common.content.teleportation.SharedInventoryManager;
import mekanism.common.util.CableUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import io.netty.buffer.ByteBuf;

public class TileEntityEntangledBlock extends TileEntityElectricBlock implements IFluidHandler, IGasHandler, ITubeConnection
{
	public SharedInventory sharedInventory;

	public static final EnumSet<EnumFacing> nothing = EnumSet.noneOf(EnumFacing.class);

	public TileEntityEntangledBlock()
	{
		super("Entangled", 0);
		inventory = new ItemStack[0];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		CableUtils.emit(this);
	}

	public void setInventory(String frequency)
	{
		sharedInventory = SharedInventoryManager.getInventory(frequency);
		markDirty();
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			switch(dataStream.readInt())
			{
				case 0:
					setInventory(PacketHandler.readString(dataStream));
					return;
			}
		}

		super.handlePacketData(dataStream);
		setEnergy(dataStream.readDouble());
	}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		super.getNetworkedData(data);
		data.add(getEnergy());
		return data;
	}

	@Override
	public EnumSet<EnumFacing> getOutputtingSides()
	{
		return sharedInventory == null ? nothing : EnumSet.of(EnumFacing.UP);
	}

	@Override
	public EnumSet<EnumFacing> getConsumingSides()
	{
		return sharedInventory == null ? nothing : EnumSet.complementOf(EnumSet.of(EnumFacing.UP));
	}

	@Override
	public double getMaxOutput()
	{
		return sharedInventory == null ? 0 : 1000;
	}

	@Override
	public double getEnergy()
	{
		return sharedInventory == null ? 0 : sharedInventory.getEnergy();
	}

	@Override
	public void setEnergy(double energy)
	{
		if(sharedInventory != null)
		{
			sharedInventory.setEnergy(energy);
		}
	}

	@Override
	public double getMaxEnergy()
	{
		return sharedInventory == null ? 0 : sharedInventory.getMaxEnergy();
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		return sharedInventory == null ? 0 : sharedInventory.fill(from, resource, doFill);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		if(sharedInventory == null)
		{
			return null;
		}
		return sharedInventory.drain(from, resource, doDrain);
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if(sharedInventory == null)
		{
			return null;
		}
		return sharedInventory.drain(from, maxDrain, doDrain);
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return sharedInventory != null && sharedInventory.canFill(from, fluid);
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return sharedInventory != null && sharedInventory.canDrain(from, fluid);
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return sharedInventory == null ? new FluidTankInfo[0] : sharedInventory.getTankInfo(from);
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		return sharedInventory == null ? 0 : sharedInventory.receiveGas(side, stack, doTransfer);
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack)
	{
		return receiveGas(side, stack, true);
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer)
	{
		return sharedInventory == null ? null : sharedInventory.drawGas(side, amount, doTransfer);
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount)
	{
		return drawGas(side, amount, true);
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return sharedInventory != null && sharedInventory.canReceiveGas(side, type);
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type)
	{
		return sharedInventory != null && sharedInventory.canDrawGas(side, type);
	}

	@Override
	public boolean canTubeConnect(EnumFacing side)
	{
		return sharedInventory != null;
	}
}
