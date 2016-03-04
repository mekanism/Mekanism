package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityQuantumEntangloporter extends TileEntityElectricBlock implements IFluidHandler, IFrequencyHandler, IGasHandler
{
	public String owner;
	
	public InventoryFrequency frequency;

	public static final EnumSet<ForgeDirection> nothing = EnumSet.noneOf(ForgeDirection.class);

	public TileEntityQuantumEntangloporter()
	{
		super("QuantumEntangloporter", 0);
		inventory = new ItemStack[0];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		CableUtils.emit(this);
	}
	
	@Override
	public Frequency getFrequency()
	{
		return frequency;
	}
	
	public FrequencyManager getManager(Frequency freq)
	{
		if(owner == null || freq == null)
		{
			return null;
		}
		
		if(freq.isPublic())
		{
			return Mekanism.publicEntangloporters;
		}
		else {
			if(!Mekanism.privateEntangloporters.containsKey(owner))
			{
				FrequencyManager manager = new FrequencyManager(InventoryFrequency.class, owner);
				Mekanism.privateEntangloporters.put(owner, manager);
				manager.createOrLoad(worldObj);
			}
			
			return Mekanism.privateEntangloporters.get(owner);
		}
	}
	
	public void setFrequency(String name, boolean publicFreq)
	{
		if(name.equals(frequency))
		{
			return;
		}
		
		FrequencyManager manager = getManager(new Frequency(name, null).setPublic(publicFreq));
		manager.deactivate(Coord4D.get(this));
		
		for(Frequency freq : manager.getFrequencies())
		{
			if(freq.name.equals(name))
			{
				frequency = (InventoryFrequency)freq;
				frequency.activeCoords.add(Coord4D.get(this));
				
				return;
			}
		}
		
		Frequency freq = new Frequency(name, owner).setPublic(publicFreq);
		freq.activeCoords.add(Coord4D.get(this));
		manager.addFrequency(freq);
		frequency = (InventoryFrequency)freq;
		
		MekanismUtils.saveChunk(this);
		markDirty();
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();
			
			if(type == 0)
			{
				String name = PacketHandler.readString(dataStream);
				boolean isPublic = dataStream.readBoolean();
				
				setFrequency(name, isPublic);
			}
			else if(type == 1)
			{
				String freq = PacketHandler.readString(dataStream);
				boolean isPublic = dataStream.readBoolean();
				
				FrequencyManager manager = getManager(new Frequency(freq, null).setPublic(isPublic));
				
				if(manager != null)
				{
					manager.remove(freq, owner);
				}
			}
			
			return;
		}

		super.handlePacketData(dataStream);
		
		setEnergy(dataStream.readDouble());
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(getEnergy());
		return data;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return frequency == null ? nothing : EnumSet.of(ForgeDirection.UP);
	}

	@Override
	public EnumSet<ForgeDirection> getConsumingSides()
	{
		return frequency == null ? nothing : EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN, ForgeDirection.UP));
	}

	@Override
	public double getMaxOutput()
	{
		return frequency == null ? 0 : InventoryFrequency.MAX_ENERGY;
	}

	@Override
	public double getEnergy()
	{
		return frequency == null ? 0 : frequency.storedEnergy;
	}

	@Override
	public void setEnergy(double energy)
	{
		if(frequency != null)
		{
			frequency.storedEnergy = Math.min(InventoryFrequency.MAX_ENERGY, energy);
		}
	}

	@Override
	public double getMaxEnergy()
	{
		return frequency == null ? 0 : frequency.MAX_ENERGY;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return frequency == null ? 0 : frequency.storedFluid.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(frequency != null && resource.isFluidEqual(frequency.storedFluid.getFluid()))
		{
			return frequency.storedFluid.drain(resource.amount, doDrain);
		}
		
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(frequency != null)
		{
			return frequency.storedFluid.drain(maxDrain, doDrain);
		}
		
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		if(frequency != null)
		{
			return frequency.storedFluid.getFluid() == null || fluid == frequency.storedFluid.getFluid().getFluid();
		}
		
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if(frequency != null)
		{
			return frequency.storedFluid.getFluid() == null || fluid == frequency.storedFluid.getFluid().getFluid();
		}
		
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return frequency == null ? PipeUtils.EMPTY : new FluidTankInfo[] {frequency.storedFluid.getInfo()};
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		return frequency == null ? 0 : frequency.storedGas.receive(stack, doTransfer);
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return receiveGas(side, stack, true);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer)
	{
		return frequency == null ? null : frequency.storedGas.draw(amount, doTransfer);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return drawGas(side, amount, true);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		if(frequency != null)
		{
			return frequency.storedGas.getGasType() == null || type == frequency.storedGas.getGasType();
		}
		
		return false;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		if(frequency != null)
		{
			return frequency.storedGas.getGasType() == null || type == frequency.storedGas.getGasType();
		}
		
		return false;
	}
	
	@Override
	public boolean handleInventory()
	{
		return false;
	}
	
	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slotID)
	{
		return frequency != null && slotID == 0 ? frequency.storedItem : null;
	}
	
	@Override
	public void setInventorySlotContents(int slotID, ItemStack itemstack)
	{
		if(frequency != null && slotID == 0)
		{
			frequency.storedItem = itemstack;
	
			if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
			{
				itemstack.stackSize = getInventoryStackLimit();
			}
		}
	}
}
