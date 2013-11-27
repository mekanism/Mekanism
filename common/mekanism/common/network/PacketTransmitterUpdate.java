package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.transmitters.ITransmitter;
import mekanism.common.tileentity.TileEntityMechanicalPipe;
import mekanism.common.tileentity.TileEntityPressurizedTube;
import mekanism.common.tileentity.TileEntityUniversalCable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.google.common.io.ByteArrayDataInput;

public class PacketTransmitterUpdate implements IMekanismPacket
{
	public PacketType packetType;
	
	public TileEntity tileEntity;
	
	public double power;
	
	public int gasType;
	public float gasScale;
	
	public int fluidType;
	public float fluidScale;
	
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
				gasType = (Integer)data[2];
				gasScale = (Float)data[3];
				break;
			case FLUID:
				fluidType = (Integer)data[2];
				fluidScale = (Float)data[3];
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
				((TileEntityUniversalCable)tileEntity).getTransmitterNetwork().clientEnergyScale = powerLevel;
			}
		}
		else if(transmitterType == 2)
	    {
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		
    		Gas gasType = GasRegistry.getGas(dataStream.readInt());
    		gasScale = dataStream.readFloat();
    		
    		if(tileEntity != null)
    		{
    			((TileEntityPressurizedTube)tileEntity).getTransmitterNetwork().refGas = gasType;
    			((TileEntityPressurizedTube)tileEntity).getTransmitterNetwork().gasScale = gasScale;
    		}
	    }
	    else if(transmitterType == 3)
	    {
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		
    		int type = dataStream.readInt();
    		Fluid fluidType = type != -1 ? FluidRegistry.getFluid(type) : null;
    		fluidScale = dataStream.readFloat();
    		
    		if(tileEntity != null)
    		{
    			((TileEntityMechanicalPipe)tileEntity).getTransmitterNetwork().refFluid = fluidType;
    			((TileEntityMechanicalPipe)tileEntity).getTransmitterNetwork().fluidScale = fluidScale;
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
				dataStream.writeInt(gasType);
				dataStream.writeFloat(gasScale);
				break;
			case FLUID:
				dataStream.writeInt(fluidType);
				dataStream.writeFloat(fluidScale);
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
