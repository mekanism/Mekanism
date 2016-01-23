package mekanism.generators.common.tile.turbine;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBasicBlock;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityTurbineRod extends TileEntityBasicBlock
{
	public List<Coord4D> rods = new ArrayList<Coord4D>();
	
	public boolean hasComplex;
	
	public String multiblockUUID;
	
	//Total blades on server, housed blades on client
	public int blades = 0;
	
	//Client stuff
	public int clientIndex;
	
	public float rotationLower;
	public float rotationUpper;
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	@Override
	public void onNeighborChange(Block block)
	{
		if(!worldObj.isRemote)
		{
			updateRods();
		}
	}
	
	public void updateRods()
	{
		if(rods.contains(Coord4D.get(this)))
		{
			rods.add(Coord4D.get(this));
		}
		
		buildRods();
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
	}
	
	private void buildRods()
	{
		List<Coord4D> newRods = new ArrayList<Coord4D>();
		int newBlades = 0;
		boolean complex = false;
		String id = null;
		
		Coord4D pointer = Coord4D.get(this);
		
		//Go to bottom rod
		while(true)
		{
			if(isRod(pointer.getFromSide(EnumFacing.DOWN)))
			{
				pointer.step(EnumFacing.DOWN);
				continue;
			}
			
			break;
		}
		
		//Put all rods in new list, top to bottom
		while(true)
		{
			newRods.add(pointer.clone());
			newBlades += ((TileEntityTurbineRod)pointer.getTileEntity(worldObj)).getHousedBlades();
			
			if(isRod(pointer.getFromSide(EnumFacing.UP)))
			{
				pointer.step(EnumFacing.UP);
				continue;
			}
			
			break;
		}
		
		if(isComplex(pointer.getFromSide(EnumFacing.UP)))
		{
			id = ((TileEntityRotationalComplex)pointer.getFromSide(EnumFacing.UP).getTileEntity(worldObj)).multiblockUUID;
			complex = true;
		}
		
		//Update all rods, send packet if necessary
		for(Coord4D coord : newRods)
		{
			TileEntityTurbineRod rod = (TileEntityTurbineRod)coord.getTileEntity(worldObj);
			int prevHoused = rod.getHousedBlades();
			int prevBlades = rod.blades;
			
			rod.rods = newRods;
			rod.blades = newBlades;
			rod.multiblockUUID = id;
			
			if(rods.indexOf(coord) == rods.size()-1)
			{
				rod.hasComplex = complex;
			}
			else {
				rod.hasComplex = false;
			}
			
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord, rod.getNetworkedData(new ArrayList())), new Range4D(coord));
		}
	}
	
	public boolean editBlade(boolean add)
	{
		if(!rods.contains(Coord4D.get(this)))
		{
			rods.add(Coord4D.get(this));
		}
		
		if((add && (rods.size()*2) - blades > 0) || (!add && (blades > 0)))
		{
			for(Coord4D coord : rods)
			{
				TileEntityTurbineRod rod = (TileEntityTurbineRod)coord.getTileEntity(worldObj);
				rod.internalEditBlade(add);
			}
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public void internalEditBlade(boolean add)
	{
		int prev = getHousedBlades();
		
		blades += add ? 1 : -1;
		
		if(getHousedBlades() != prev)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
		}
	}
	
	public int getHousedBlades()
	{
		if(!worldObj.isRemote)
		{
			if(rods.size() > 0)
			{
				return Math.max(0, Math.min(2, blades - (rods.indexOf(Coord4D.get(this)))*2));
			}
			else {
				return blades;
			}
		}
		else {
			return blades;
		}
	}
	
	private boolean isRod(Coord4D coord)
	{
		return coord.getTileEntity(worldObj) instanceof TileEntityTurbineRod;
	}
	
	private boolean isComplex(Coord4D coord)
	{
		return coord.getTileEntity(worldObj) instanceof TileEntityRotationalComplex;
	}
	
	@Override
	public void onChunkLoad()
	{
		super.onChunkLoad();
		
		if(!worldObj.isRemote)
		{
			updateRods();
		}
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		int prevBlades = blades;
		int prevIndex = clientIndex;
		
		blades = dataStream.readInt();
		clientIndex = dataStream.readInt();
		
		if(dataStream.readBoolean())
		{
			multiblockUUID = PacketHandler.readString(dataStream);
		}
		else {
			multiblockUUID = null;
		}
		
		if(prevBlades != blades || prevIndex != clientIndex)
		{
			rotationLower = 0;
			rotationUpper = 0;
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(getHousedBlades());
		data.add(rods.indexOf(Coord4D.get(this)));
		
		if(multiblockUUID != null)
		{
			data.add(true);
			data.add(multiblockUUID);
		}
		else {
			data.add(false);
		}
		
		return data;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		blades = nbtTags.getInteger("blades");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("blades", getHousedBlades());
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public void onUpdate() {}
}
