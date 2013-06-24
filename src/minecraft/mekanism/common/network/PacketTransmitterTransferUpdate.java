package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.EnumGas;
import mekanism.api.GasTransferProtocol;
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
			case GAS:
				gasName = ((EnumGas)data[2]).name;
				break;
			case LIQUID:
				liquidStack = (LiquidStack)data[2];
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
    		EnumGas type = EnumGas.getFromName(dataStream.readUTF());
    		
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		
    		if(tileEntity != null)
    		{
    			new GasTransferProtocol(tileEntity, null, type, 0).clientUpdate();
    		}
	    }
	    else if(transmitterType == 1)
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
		GAS,
		LIQUID
	}
}
