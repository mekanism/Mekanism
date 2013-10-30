package mekanism.common;

import java.util.ArrayList;

import mekanism.api.SideData;
import mekanism.common.tileentity.TileEntityContainerBlock;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.io.ByteArrayDataInput;

public class TileComponentEjector implements ITileComponent
{
	public TileEntityContainerBlock tileEntity;
	
	public SideData sideData;
	
	public TileComponentEjector(TileEntityContainerBlock tile, SideData data)
	{
		tileEntity = tile;
		sideData = data;
		
		tile.components.add(this);
	}
	
	public void onOutput()
	{
		
	}

	@Override
	public void tick() {}

	@Override
	public void read(NBTTagCompound nbtTags) {}

	@Override
	public void read(ByteArrayDataInput dataStream) {}

	@Override
	public void write(NBTTagCompound nbtTags) {}
	
	@Override
	public void write(ArrayList data) {}
}
