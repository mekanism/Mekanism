package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.gas.EnumGas;
import mekanism.client.EnergyClientUpdate;
import mekanism.client.GasClientUpdate;
import mekanism.client.FluidClientUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.io.ByteArrayDataInput;

public class PacketTransmitterTransferUpdate implements IMekanismPacket
{
	public TransmitterTransferType activeType;
	
	public TileEntity tileEntity;
	
	public double power;
	
	public String gasName;
	
	public FluidStack fluidStack;
	
	@Override
	public String getName() 
	{
		return "TransmitterTransferUpdate";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		activeType = (TransmitterTransferType)data[0];
		tileEntity = (TileEntity)data[1];
		
		switch(activeType)
		{
			case ENERGY:
				power = (Double)data[2];
				break;
			case GAS:
				gasName = ((EnumGas)data[2]).name;
				break;
			case FLUID:
				fluidStack = (FluidStack)data[2];
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
			double powerLevel = dataStream.readDouble();
			
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			
			if(tileEntity != null)
			{
				new EnergyClientUpdate(tileEntity, powerLevel).clientUpdate();
			}
		}
		else if(transmitterType == 1)
	    {
    		EnumGas type = EnumGas.getFromName(dataStream.readUTF());
    		
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		
    		if(tileEntity != null)
    		{
    			new GasClientUpdate(tileEntity, type).clientUpdate();
    		}
	    }
	    else if(transmitterType == 2)
	    {
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		FluidStack fluidStack = new FluidStack(dataStream.readInt(), dataStream.readInt());
    		
    		if(tileEntity != null)
    		{
    			new FluidClientUpdate(tileEntity, fluidStack).clientUpdate();
    		}
	    }
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception 
	{
		dataStream.writeInt(activeType.ordinal());
		
		dataStream.writeInt(tileEntity.xCoord);
		dataStream.writeInt(tileEntity.yCoord);
		dataStream.writeInt(tileEntity.zCoord);
		
		switch(activeType)
		{
			case ENERGY:
				dataStream.writeDouble(power);
				break;
			case GAS:
				dataStream.writeUTF(gasName);
				break;
			case FLUID:
				dataStream.writeInt(fluidStack.fluidID);
				dataStream.writeInt(fluidStack.amount);
				break;
		}
	}
	
	public static enum TransmitterTransferType
	{
		ENERGY,
		GAS,
		FLUID
	}
}
