package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.SideData.IOState;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITankManager;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.integration.IComputerIntegration;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.CableUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityQuantumEntangloporter extends TileEntityElectricBlock implements ISideConfiguration, ITankManager, IFluidHandler, IFrequencyHandler, IGasHandler, IHeatTransfer, ITubeConnection, IComputerIntegration, ISecurityTile
{
	public InventoryFrequency frequency;
	
	public double heatToAbsorb = 0;
	
	public double lastTransferLoss;
	public double lastEnvironmentLoss;
	
	public List<Frequency> publicCache = new ArrayList<Frequency>();
	public List<Frequency> privateCache = new ArrayList<Frequency>();

	public static final EnumSet<ForgeDirection> nothing = EnumSet.noneOf(ForgeDirection.class);
	
	public TileComponentEjector ejectorComponent;
	public TileComponentConfig configComponent;
	public TileComponentSecurity securityComponent;

	public TileEntityQuantumEntangloporter()
	{
		super("QuantumEntangloporter", 0);
		
		configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.GAS, TransmissionType.ENERGY, TransmissionType.HEAT);
		
		for(TransmissionType type : TransmissionType.values())
		{
			if(type != TransmissionType.HEAT)
			{
				configComponent.setIOConfig(type);
			}
			else {
				configComponent.setInputConfig(type);
			}
		}

		inventory = new ItemStack[0];
		
		configComponent.getOutputs(TransmissionType.ITEM).get(2).availableSlots = new int[] {0};
		configComponent.getOutputs(TransmissionType.FLUID).get(2).availableSlots = new int[] {0};
		configComponent.getOutputs(TransmissionType.GAS).get(2).availableSlots = new int[] {1};
		
		ejectorComponent = new TileComponentEjector(this);
		ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
		ejectorComponent.setOutputData(TransmissionType.FLUID, configComponent.getOutputs(TransmissionType.FLUID).get(2));
		ejectorComponent.setOutputData(TransmissionType.GAS, configComponent.getOutputs(TransmissionType.GAS).get(2));
		
		securityComponent = new TileComponentSecurity(this);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			if(configComponent.isEjecting(TransmissionType.ENERGY))
			{
				CableUtils.emit(this);
			}
			
			double[] loss = simulateHeat();
			applyTemperatureChange();
			
			lastTransferLoss = loss[0];
			lastEnvironmentLoss = loss[1];
			
			FrequencyManager manager = getManager(frequency);
			Frequency lastFreq = frequency;
			
			if(manager != null)
			{
				if(frequency != null && !frequency.valid)
				{
					frequency = (InventoryFrequency)manager.validateFrequency(getSecurity().getOwner(), Coord4D.get(this), frequency);
					markDirty();
				}
				
				if(frequency != null)
				{
					frequency = (InventoryFrequency)manager.update(getSecurity().getOwner(), Coord4D.get(this), frequency);
					
					if(frequency == null)
					{
						markDirty();
					}
				}
			}
			else {
				frequency = null;
				
				if(lastFreq != null)
				{
					markDirty();
				}
			}
		}
	}
	
	private boolean hasFrequency()
	{
		return frequency != null && frequency.valid;
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		
		if(!worldObj.isRemote)
		{
			if(frequency != null)
			{
				FrequencyManager manager = getManager(frequency);
				
				if(manager != null)
				{
					manager.deactivate(Coord4D.get(this));
				}
			}
		}
	}
	
	@Override
	public Frequency getFrequency(FrequencyManager manager)
	{
		if(manager == Mekanism.securityFrequencies)
		{
			return getSecurity().getFrequency();
		}
		
		return frequency;
	}
	
	public FrequencyManager getManager(Frequency freq)
	{
		if(getSecurity().getOwner() == null || freq == null)
		{
			return null;
		}
		
		if(freq.isPublic())
		{
			return Mekanism.publicEntangloporters;
		}
		else {
			if(!Mekanism.privateEntangloporters.containsKey(getSecurity().getOwner()))
			{
				FrequencyManager manager = new FrequencyManager(InventoryFrequency.class, InventoryFrequency.ENTANGLOPORTER, getSecurity().getOwner());
				Mekanism.privateEntangloporters.put(getSecurity().getOwner(), manager);
				manager.createOrLoad(worldObj);
			}
			
			return Mekanism.privateEntangloporters.get(getSecurity().getOwner());
		}
	}
	
	public void setFrequency(String name, boolean publicFreq)
	{
		FrequencyManager manager = getManager(new InventoryFrequency(name, null).setPublic(publicFreq));
		manager.deactivate(Coord4D.get(this));
		
		for(Frequency freq : manager.getFrequencies())
		{
			if(freq.name.equals(name))
			{
				frequency = (InventoryFrequency)freq;
				frequency.activeCoords.add(Coord4D.get(this));
				
				markDirty();
				
				return;
			}
		}
		
		Frequency freq = new InventoryFrequency(name, getSecurity().getOwner()).setPublic(publicFreq);
		freq.activeCoords.add(Coord4D.get(this));
		manager.addFrequency(freq);
		frequency = (InventoryFrequency)freq;
		
		MekanismUtils.saveChunk(this);
		markDirty();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		if(nbtTags.hasKey("frequency"))
		{
			frequency = new InventoryFrequency(nbtTags.getCompoundTag("frequency"));
			frequency.valid = false;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		if(frequency != null)
		{
			NBTTagCompound frequencyTag = new NBTTagCompound();
			frequency.write(frequencyTag);
			nbtTags.setTag("frequency", frequencyTag);
		}
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
				
				FrequencyManager manager = getManager(new InventoryFrequency(freq, null).setPublic(isPublic));
				
				if(manager != null)
				{
					manager.remove(freq, getSecurity().getOwner());
				}
			}
			
			return;
		}

		super.handlePacketData(dataStream);
		
		if(worldObj.isRemote)
		{
			lastTransferLoss = dataStream.readDouble();
			lastEnvironmentLoss = dataStream.readDouble();
			
			if(dataStream.readBoolean())
			{
				frequency = new InventoryFrequency(dataStream);
			}
			else {
				frequency = null;
			}
			
			publicCache.clear();
			privateCache.clear();
			
			int amount = dataStream.readInt();
			
			for(int i = 0; i < amount; i++)
			{
				publicCache.add(new InventoryFrequency(dataStream));
			}
			
			amount = dataStream.readInt();
			
			for(int i = 0; i < amount; i++)
			{
				privateCache.add(new InventoryFrequency(dataStream));
			}
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(lastTransferLoss);
		data.add(lastEnvironmentLoss);
		
		if(frequency != null)
		{
			data.add(true);
			frequency.write(data);
		}
		else {
			data.add(false);
		}
		
		data.add(Mekanism.publicEntangloporters.getFrequencies().size());
		
		for(Frequency freq : Mekanism.publicEntangloporters.getFrequencies())
		{
			freq.write(data);
		}
		
		FrequencyManager manager = getManager(new InventoryFrequency(null, null).setPublic(false));
		
		if(manager != null)
		{
			data.add(manager.getFrequencies().size());
			
			for(Frequency freq : manager.getFrequencies())
			{
				freq.write(data);
			}
		}
		else {
			data.add(0);
		}
		
		return data;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return !hasFrequency() ? nothing : configComponent.getSidesForData(TransmissionType.ENERGY, facing, 2);
	}

	@Override
	public EnumSet<ForgeDirection> getConsumingSides()
	{
		return !hasFrequency() ? nothing : configComponent.getSidesForData(TransmissionType.ENERGY, facing, 1);
	}

	@Override
	public double getMaxOutput()
	{
		return !hasFrequency() ? 0 : InventoryFrequency.MAX_ENERGY;
	}

	@Override
	public double getEnergy()
	{
		return !hasFrequency() ? 0 : frequency.storedEnergy;
	}

	@Override
	public void setEnergy(double energy)
	{
		if(hasFrequency())
		{
			frequency.storedEnergy = Math.min(InventoryFrequency.MAX_ENERGY, energy);
		}
	}

	@Override
	public double getMaxEnergy()
	{
		return !hasFrequency() ? 0 : frequency.MAX_ENERGY;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return !hasFrequency() ? 0 : frequency.storedFluid.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(hasFrequency() && resource.isFluidEqual(frequency.storedFluid.getFluid()))
		{
			return frequency.storedFluid.drain(resource.amount, doDrain);
		}
		
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(hasFrequency())
		{
			return frequency.storedFluid.drain(maxDrain, doDrain);
		}
		
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		if(hasFrequency() && configComponent.getOutput(TransmissionType.FLUID, from.ordinal(), facing).ioState == IOState.INPUT)
		{
			return frequency.storedFluid.getFluid() == null || fluid == frequency.storedFluid.getFluid().getFluid();
		}
		
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if(hasFrequency() && configComponent.getOutput(TransmissionType.FLUID, from.ordinal(), facing).ioState == IOState.OUTPUT)
		{
			return frequency.storedFluid.getFluid() == null || fluid == frequency.storedFluid.getFluid().getFluid();
		}
		
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(hasFrequency())
		{
			if(configComponent.getOutput(TransmissionType.FLUID, from.ordinal(), facing).ioState != IOState.OFF)
			{
				return new FluidTankInfo[] {frequency.storedFluid.getInfo()};
			}
		}
		
		return PipeUtils.EMPTY;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		return !hasFrequency() ? 0 : frequency.storedGas.receive(stack, doTransfer);
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return receiveGas(side, stack, true);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer)
	{
		return !hasFrequency() ? null : frequency.storedGas.draw(amount, doTransfer);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return drawGas(side, amount, true);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		if(hasFrequency() && configComponent.getOutput(TransmissionType.GAS, side.ordinal(), facing).ioState == IOState.INPUT)
		{
			return frequency.storedGas.getGasType() == null || type == frequency.storedGas.getGasType();
		}
		
		return false;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		if(hasFrequency() && configComponent.getOutput(TransmissionType.GAS, side.ordinal(), facing).ioState == IOState.OUTPUT)
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
		return hasFrequency() && slotID == 0 ? frequency.storedItem : null;
	}
	
	@Override
	public void setInventorySlotContents(int slotID, ItemStack itemstack)
	{
		if(hasFrequency() && slotID == 0)
		{
			frequency.storedItem = itemstack;
	
			if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
			{
				itemstack.stackSize = getInventoryStackLimit();
			}
		}
	}

	@Override
	public double getTemp() 
	{
		return hasFrequency() ? frequency.temperature : 0;
	}

	@Override
	public double getInverseConductionCoefficient() 
	{
		return 1;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side) 
	{
		return 1000;
	}

	@Override
	public void transferHeatTo(double heat) 
	{
		heatToAbsorb += heat;
	}

	@Override
	public double[] simulateHeat()
	{
		return HeatUtils.simulate(this);
	}

	@Override
	public double applyTemperatureChange() 
	{
		if(hasFrequency())
		{
			frequency.temperature += heatToAbsorb;
		}
		
		heatToAbsorb = 0;
		
		return hasFrequency() ? frequency.temperature : 0;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side) 
	{
		return hasFrequency() && configComponent.getOutput(TransmissionType.HEAT, side.ordinal(), facing).ioState != IOState.OFF;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side) 
	{
		TileEntity adj = Coord4D.get(this).getFromSide(side).getTileEntity(worldObj);
		
		if(hasFrequency() && configComponent.getOutput(TransmissionType.HEAT, side.ordinal(), facing).ioState == IOState.INPUT)
		{
			if(adj instanceof IHeatTransfer)
			{
				return (IHeatTransfer)adj;
			}
		}
		
		return null;
	}
	
	@Override
	public boolean canInsertItem(int slotID, ItemStack itemstack, int side)
	{
		return hasFrequency() && configComponent.getOutput(TransmissionType.ITEM, side, facing).ioState == IOState.INPUT;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(hasFrequency() && configComponent.getOutput(TransmissionType.ITEM, side, facing).ioState != IOState.OFF)
		{
			return new int[] {0};
		}
		
		return InventoryUtils.EMPTY;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		return hasFrequency() && configComponent.getOutput(TransmissionType.ITEM, side, facing).ioState == IOState.OUTPUT;
	}

	@Override
	public Object[] getTanks() 
	{
		if(!hasFrequency())
		{
			return null;
		}
		
		return new Object[] {frequency.storedFluid, frequency.storedGas};
	}

	@Override
	public TileComponentConfig getConfig() 
	{
		return configComponent;
	}

	@Override
	public int getOrientation() 
	{
		return facing;
	}

	@Override
	public TileComponentEjector getEjector() 
	{
		return ejectorComponent;
	}
	
	@Override
	public TileComponentSecurity getSecurity()
	{
		return securityComponent;
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side) 
	{
		return hasFrequency() && configComponent.getOutput(TransmissionType.GAS, side.ordinal(), facing).ioState != IOState.OFF;
	}
	
	private static final String[] methods = new String[] {"setFrequency"};

	@Override
	public String[] getMethods()
	{
		return methods;
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws Exception
	{
		switch(method)
		{
			case 0:
				if(!(arguments[0] instanceof String) || !(arguments[1] instanceof Boolean))
				{
					return new Object[] {"Invalid parameters."};
				}
				
				String freq = ((String)arguments[0]).trim();
				boolean isPublic = (Boolean)arguments[1];
				
				setFrequency(freq, isPublic);
				
				return new Object[] {"Frequency set."};
			default:
				throw new NoSuchMethodException();
		}
	}
}
