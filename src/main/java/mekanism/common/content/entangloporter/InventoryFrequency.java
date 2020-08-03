package mekanism.common.content.entangloporter;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.frequency.Frequency;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class InventoryFrequency extends Frequency
{
	public static final String ENTANGLOPORTER = "Entangloporter";
	
	public static final double MAX_ENERGY = 1000000000;
	
	public double storedEnergy;
	public FluidTank storedFluid;
	public GasTank storedGas;
	public ItemStack storedItem;
	public double temperature;
	
	public InventoryFrequency(String n, String o)
	{
		super(n, o);
		
		storedFluid = new FluidTank(1000);
		storedGas = new GasTank(1000);
	}
	
	public InventoryFrequency(NBTTagCompound nbtTags)
	{
		super(nbtTags);
	}
	
	public InventoryFrequency(ByteBuf dataStream)
	{
		super(dataStream);
	}
	
	@Override
	public void write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);
		
		nbtTags.setDouble("storedEnergy", storedEnergy);
		
		if(storedFluid.getFluid() != null)
		{
			nbtTags.setTag("storedFluid", storedFluid.writeToNBT(new NBTTagCompound()));
		}
		
		if(storedGas.getGas() != null)
		{
			nbtTags.setTag("storedGas", storedGas.write(new NBTTagCompound()));
		}
		
		if(storedItem != null)
		{
			nbtTags.setTag("storedItem", storedItem.writeToNBT(new NBTTagCompound()));
		}
		
		nbtTags.setDouble("temperature", temperature);
	}

	@Override
	protected void read(NBTTagCompound nbtTags)
	{
		super.read(nbtTags);
		
		storedFluid = new FluidTank(1000);
		storedGas = new GasTank(1000);
		
		storedEnergy = nbtTags.getDouble("storedEnergy");
		
		if(nbtTags.hasKey("storedFluid"))
		{
			storedFluid.readFromNBT(nbtTags.getCompoundTag("storedFluid"));
		}
		
		if(nbtTags.hasKey("storedGas"))
		{
			storedGas.read(nbtTags.getCompoundTag("storedGas"));
		}
		
		if(nbtTags.hasKey("storedItem"))
		{
			storedItem = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("storedItem"));
		}
		
		temperature = nbtTags.getDouble("temperature");
	}

	@Override
	public void write(ArrayList data)
	{
		super.write(data);
		
		data.add(storedEnergy);
		
		if(storedFluid.getFluid() != null)
		{
			data.add(true);
			data.add(storedFluid.getFluid().getFluidID());
			data.add(storedFluid.getFluidAmount());
		}
		else {
			data.add(false);
		}
		
		if(storedGas.getGas() != null)
		{
			data.add(true);
			data.add(storedGas.getGasType().getID());
			data.add(storedGas.getStored());
		}
		else {
			data.add(false);
		}
		
		data.add(temperature);
	}

	@Override
	protected void read(ByteBuf dataStream)
	{
		super.read(dataStream);
		
		storedFluid = new FluidTank(1000);
		storedGas = new GasTank(1000);
		
		storedEnergy = dataStream.readDouble();
		
		if(dataStream.readBoolean())
		{
			storedFluid.setFluid(new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			storedFluid.setFluid(null);
		}
		
		if(dataStream.readBoolean())
		{
			storedGas.setGas(new GasStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			storedGas.setGas(null);
		}
		
		temperature = dataStream.readDouble();
	}
}
