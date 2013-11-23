package mekanism.common.tileentity;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import mekanism.common.transporter.TransporterStack;
import mekanism.common.util.TransporterUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class TileEntityDiversionTransporter extends TileEntityLogisticalTransporter
{
	public int[] modes = {0, 0, 0, 0, 0, 0};
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		modes = nbtTags.getIntArray("modes");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		nbtTags.setIntArray("modes", modes);
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		modes[0] = dataStream.readInt();
		modes[1] = dataStream.readInt();
		modes[2] = dataStream.readInt();
		modes[3] = dataStream.readInt();
		modes[4] = dataStream.readInt();
		modes[5] = dataStream.readInt();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data = super.getNetworkedData(data);
		
		data.add(modes[0]);
		data.add(modes[1]);
		data.add(modes[2]);
		data.add(modes[3]);
		data.add(modes[4]);
		data.add(modes[5]);
		
		return data;
	}
	
	@Override
	public ArrayList getSyncPacket(TransporterStack stack, boolean kill)
	{
		ArrayList data = super.getSyncPacket(stack, kill);
		
		data.add(modes[0]);
		data.add(modes[1]);
		data.add(modes[2]);
		data.add(modes[3]);
		data.add(modes[4]);
		data.add(modes[5]);
		
		return data;
	}
}
