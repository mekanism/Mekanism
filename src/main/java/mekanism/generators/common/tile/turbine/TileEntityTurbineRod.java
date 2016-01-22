package mekanism.generators.common.tile.turbine;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBasicBlock;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityTurbineRod extends TileEntityBasicBlock
{
	public List<Coord4D> rods = new ArrayList<Coord4D>();
	
	//Total blades on server, housed blades on client
	public int blades = 0;
	
	public int clientIndex;
	
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
	
	private void updateRods()
	{
		Coord4D current = Coord4D.get(this);
		Coord4D up = current.getFromSide(ForgeDirection.UP);
		Coord4D down = current.getFromSide(ForgeDirection.DOWN);
		
		if(!rods.contains(current))
		{
			rods.add(current);
		}
		
		if((isRod(up) != !rods.contains(up)) || (isRod(down) != !rods.contains(down)))
		{
			buildRods();
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
		}
	}
	
	private void buildRods()
	{
		List<Coord4D> newRods = new ArrayList<Coord4D>();
		int newBlades = 0;
		
		Coord4D pointer = Coord4D.get(this);
		
		//Go to bottom rod
		while(true)
		{
			if(isRod(pointer.getFromSide(ForgeDirection.DOWN)))
			{
				pointer.step(ForgeDirection.DOWN);
				continue;
			}
			
			break;
		}
		
		//Put all rods in new list, top to bottom
		while(true)
		{
			newRods.add(pointer.clone());
			newBlades += ((TileEntityTurbineRod)pointer.getTileEntity(worldObj)).getHousedBlades();
			
			if(isRod(pointer.getFromSide(ForgeDirection.UP)))
			{
				pointer.step(ForgeDirection.UP);
				continue;
			}
			
			break;
		}
		
		//Update all rods, send packet if necessary
		for(Coord4D coord : newRods)
		{
			TileEntityTurbineRod rod = (TileEntityTurbineRod)coord.getTileEntity(worldObj);
			int prevHoused = rod.getHousedBlades();
			int prevBlades = rod.blades;
			
			rod.rods = newRods;
			rod.blades = newBlades;
			
			if(rod.getHousedBlades() != prevHoused || rod.blades != prevBlades)
			{
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord, rod.getNetworkedData(new ArrayList())), new Range4D(coord));
			}
		}
	}
	
	public boolean editBlade(boolean add)
	{
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
		
		blades = dataStream.readInt();
		clientIndex = dataStream.readInt();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(getHousedBlades());
		data.add(rods.indexOf(Coord4D.get(this)));
		
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
