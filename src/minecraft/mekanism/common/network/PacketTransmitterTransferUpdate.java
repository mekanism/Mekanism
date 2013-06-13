package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.EnumGas;
import mekanism.api.GasTransferProtocol;
import mekanism.common.EnergyTransferProtocol;
import mekanism.common.LiquidTransferProtocol;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.liquids.LiquidStack;

import com.google.common.io.ByteArrayDataInput;

public class PacketTransmitterTransferUpdate implements IMekanismPacket
{
	public TransmitterTransferType activeType;
	
	public TileEntity tileEntity;
	
	public String gasName;
	
	public LiquidStack liquidStack;
	
	public PacketTransmitterTransferUpdate(TransmitterTransferType type, Object... params)
	{
		tileEntity = (TileEntity)params[0];
		
		activeType = type;
		
		switch(type)
		{
			case ENERGY:
				break;
			case GAS:
				gasName = ((EnumGas)params[1]).name;
				break;
			case LIQUID:
				liquidStack = (LiquidStack)params[1];
				break;
		}
	}
	
	public PacketTransmitterTransferUpdate() {}
	
	@Override
	public String getName() 
	{
		return "TransmitterTransferUpdate";
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
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		
    		if(tileEntity != null)
    		{
    			new EnergyTransferProtocol(tileEntity, null, new ArrayList()).clientUpdate();
    		}
	    }
	    else if(transmitterType == 1)
	    {
    		EnumGas type = EnumGas.getFromName(dataStream.readUTF());
    		
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		
    		if(tileEntity != null)
    		{
    			new GasTransferProtocol(tileEntity, null, type, 0).clientUpdate();
    		}
	    }
	    else if(transmitterType == 2)
	    {
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		LiquidStack liquidStack = new LiquidStack(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
    		
    		if(tileEntity != null)
    		{
    			new LiquidTransferProtocol(tileEntity, null, liquidStack).clientUpdate();
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
				break;
			case GAS:
				dataStream.writeUTF(gasName);
				break;
			case LIQUID:
				dataStream.writeInt(liquidStack.itemID);
				dataStream.writeInt(liquidStack.amount);
				dataStream.writeInt(liquidStack.itemMeta);
				break;
		}
	}
	
	public static enum TransmitterTransferType
	{
		ENERGY,
		GAS,
		LIQUID
	}
}
