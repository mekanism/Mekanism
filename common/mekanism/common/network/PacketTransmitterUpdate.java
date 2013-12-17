package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasNetwork;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.transmitters.ITransmitter;
import mekanism.common.EnergyNetwork;
import mekanism.common.FluidNetwork;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.io.ByteArrayDataInput;

public class PacketTransmitterUpdate implements IMekanismPacket
{
	public PacketType packetType;
	
	public TileEntity tileEntity;
	
	public double power;
	
	public GasStack gasStack;
	public boolean didGasTransfer;
	
	public FluidStack fluidStack;
	public boolean didFluidTransfer;
	
	@Override
	public String getName() 
	{
		return "TransmitterUpdate";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		packetType = (PacketType)data[0];
		tileEntity = (TileEntity)data[1];
		
		switch(packetType)
		{
			case ENERGY:
				power = (Double)data[2];
				break;
			case GAS:
				gasStack = (GasStack)data[2];
				didGasTransfer = (Boolean)data[3];
				break;
			case FLUID:
				fluidStack = (FluidStack)data[2];
				didFluidTransfer = (Boolean)data[3];
				break;
		}
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		int transmitterType = dataStream.readInt();
		
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();
		
		if(transmitterType == 0)
		{
			ITransmitter transmitter = (ITransmitter)world.getBlockTileEntity(x, y, z);
			
			if(transmitter != null)
			{
				transmitter.refreshTransmitterNetwork();
			}
		}
		if(transmitterType == 1)
		{
			double powerLevel = dataStream.readDouble();
			
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			
			if(tileEntity != null)
			{
				((ITransmitter<EnergyNetwork>)tileEntity).getTransmitterNetwork().clientEnergyScale = powerLevel;
			}
		}
		else if(transmitterType == 2)
	    {
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		
    		Gas gasType = GasRegistry.getGas(dataStream.readInt());
    		int amount = dataStream.readInt();
    		didGasTransfer = dataStream.readBoolean();
    		
    		if(tileEntity != null)
    		{
    			if(gasType != null)
    			{
    				((ITransmitter<GasNetwork>)tileEntity).getTransmitterNetwork().gasStored = new GasStack(gasType, amount);
    			}
    			else {
    				if(((ITransmitter<GasNetwork>)tileEntity).getTransmitterNetwork().gasStored != null)
    				{
    					((ITransmitter<GasNetwork>)tileEntity).getTransmitterNetwork().gasStored.amount = amount;
    				}
    			}
    			
    			((ITransmitter<GasNetwork>)tileEntity).getTransmitterNetwork().didTransfer = didGasTransfer;
    		}
	    }
	    else if(transmitterType == 3)
	    {
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		
    		int type = dataStream.readInt();
    		Fluid fluidType = type != -1 ? FluidRegistry.getFluid(type) : null;
    		int amount = dataStream.readInt();
    		FluidStack stack = null;
    		didFluidTransfer = dataStream.readBoolean();
    		
    		if(fluidType != null)
    		{
    			stack = new FluidStack(fluidType, amount);
    		}
    		
    		if(tileEntity != null)
    		{
    			if(fluidType != null)
    			{
    				((ITransmitter<FluidNetwork>)tileEntity).getTransmitterNetwork().refFluid = fluidType;
    			}
    			
    			((ITransmitter<FluidNetwork>)tileEntity).getTransmitterNetwork().fluidStored = stack;
    			((ITransmitter<FluidNetwork>)tileEntity).getTransmitterNetwork().didTransfer = didFluidTransfer;
    		}
	    }
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception 
	{
		dataStream.writeInt(packetType.ordinal());
		
		dataStream.writeInt(tileEntity.xCoord);
		dataStream.writeInt(tileEntity.yCoord);
		dataStream.writeInt(tileEntity.zCoord);
		
		switch(packetType)
		{
			case ENERGY:
				dataStream.writeDouble(power);
				break;
			case GAS:
				dataStream.writeInt(gasStack != null ? gasStack.getGas().getID() : -1);
				dataStream.writeInt(gasStack != null ? gasStack.amount : 0);
				dataStream.writeBoolean(didGasTransfer);
				break;
			case FLUID:
				dataStream.writeInt(fluidStack != null ? fluidStack.getFluid().getID() : -1);
				dataStream.writeInt(fluidStack != null ? fluidStack.amount : 0);
				dataStream.writeBoolean(didFluidTransfer);
				break;
		}
	}
	
	public static enum PacketType
	{
		UPDATE,
		ENERGY,
		GAS,
		FLUID
	}
}
