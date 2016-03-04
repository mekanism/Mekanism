package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IHeatTransfer;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.SideData;
import mekanism.common.SideData.IOState;
import mekanism.common.base.IEjector;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITankManager;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.CableUtils;
import mekanism.common.util.HeatUtils;
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

public class TileEntityQuantumEntangloporter extends TileEntityElectricBlock implements ISideConfiguration, ITankManager, IFluidHandler, IFrequencyHandler, IGasHandler, IHeatTransfer
{
	public String owner;
	
	public InventoryFrequency frequency;
	
	public double heatToAbsorb = 0;
	
	public double lastTransferLoss;
	public double lastEnvironmentLoss;
	
	public List<Frequency> publicCache = new ArrayList<Frequency>();
	public List<Frequency> privateCache = new ArrayList<Frequency>();

	public static final EnumSet<ForgeDirection> nothing = EnumSet.noneOf(ForgeDirection.class);
	
	public TileComponentEjector ejectorComponent;
	public TileComponentConfig configComponent;

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
		
		ejectorComponent = new TileComponentEjector(this);
		ejectorComponent.setOutputData(TransmissionType.ITEM, new SideData("dummy", EnumColor.GREY, new int[] {0}));
		ejectorComponent.setOutputData(TransmissionType.FLUID, new SideData("dummy", EnumColor.GREY, new int[] {0}));
		ejectorComponent.setOutputData(TransmissionType.GAS, new SideData("dummy", EnumColor.GREY, new int[] {1}));
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
			
			if(manager != null)
			{
				if(frequency != null && !frequency.valid)
				{
					frequency = (InventoryFrequency)manager.validateFrequency(owner, Coord4D.get(this), frequency);
				}
				
				if(frequency != null)
				{
					frequency = (InventoryFrequency)manager.update(owner, Coord4D.get(this), frequency);
				}
			}
			else {
				frequency = null;
			}
		}
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
		
		Frequency freq = new InventoryFrequency(name, owner).setPublic(publicFreq);
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
		
		if(nbtTags.hasKey("owner"))
		{
			owner = nbtTags.getString("owner");
		}
		
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
		
		if(owner != null)
		{
			nbtTags.setString("owner", owner);
		}
		
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
				
				FrequencyManager manager = getManager(new Frequency(freq, null).setPublic(isPublic));
				
				if(manager != null)
				{
					manager.remove(freq, owner);
				}
			}
			
			return;
		}

		super.handlePacketData(dataStream);
		
		lastTransferLoss = dataStream.readDouble();
		lastEnvironmentLoss = dataStream.readDouble();
		
		if(dataStream.readBoolean())
		{
			owner = PacketHandler.readString(dataStream);
		}
		else {
			owner = null;
		}
		
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
			publicCache.add(new Frequency(dataStream));
		}
		
		amount = dataStream.readInt();
		
		for(int i = 0; i < amount; i++)
		{
			privateCache.add(new Frequency(dataStream));
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(lastTransferLoss);
		data.add(lastEnvironmentLoss);
		
		if(owner != null)
		{
			data.add(true);
			data.add(owner);
		}
		else {
			data.add(false);
		}
		
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
		
		FrequencyManager manager = getManager(new Frequency(null, null).setPublic(false));
		
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
		return frequency == null ? nothing : configComponent.getSidesForData(TransmissionType.ENERGY, facing, 2);
	}

	@Override
	public EnumSet<ForgeDirection> getConsumingSides()
	{
		return frequency == null ? nothing : configComponent.getSidesForData(TransmissionType.ENERGY, facing, 1);
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
		if(frequency != null && configComponent.getOutput(TransmissionType.FLUID, from.ordinal(), facing).ioState == IOState.INPUT)
		{
			return frequency.storedFluid.getFluid() == null || fluid == frequency.storedFluid.getFluid().getFluid();
		}
		
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if(frequency != null && configComponent.getOutput(TransmissionType.FLUID, from.ordinal(), facing).ioState == IOState.OUTPUT)
		{
			return frequency.storedFluid.getFluid() == null || fluid == frequency.storedFluid.getFluid().getFluid();
		}
		
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(frequency != null)
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
		if(frequency != null && configComponent.getOutput(TransmissionType.GAS, side.ordinal(), facing).ioState == IOState.INPUT)
		{
			return frequency.storedGas.getGasType() == null || type == frequency.storedGas.getGasType();
		}
		
		return false;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		if(frequency != null && configComponent.getOutput(TransmissionType.GAS, side.ordinal(), facing).ioState == IOState.OUTPUT)
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

	@Override
	public double getTemp() 
	{
		return frequency != null ? frequency.temperature : 0;
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
		if(frequency != null)
		{
			frequency.temperature += heatToAbsorb;
		}
		
		heatToAbsorb = 0;
		
		return frequency != null ? frequency.temperature : 0;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side) 
	{
		return frequency != null && configComponent.getOutput(TransmissionType.HEAT, side.ordinal(), facing).ioState != IOState.OFF;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side) 
	{
		TileEntity adj = Coord4D.get(this).getFromSide(side).getTileEntity(worldObj);
		
		if(configComponent.getOutput(TransmissionType.HEAT, side.ordinal(), facing).ioState == IOState.INPUT)
		{
			if(adj instanceof IHeatTransfer)
			{
				return (IHeatTransfer)adj;
			}
		}
		
		return null;
	}

	@Override
	public Object[] getTanks() 
	{
		if(frequency == null)
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
	public IEjector getEjector() 
	{
		return ejectorComponent;
	}
}
