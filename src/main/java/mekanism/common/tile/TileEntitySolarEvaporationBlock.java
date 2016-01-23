package mekanism.common.tile;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntitySolarEvaporationBlock extends TileEntityContainerBlock
{
	public Coord4D master;
	
	public boolean attempted;

	public TileEntitySolarEvaporationBlock()
	{
		super("SolarEvaporationBlock");

		inventory = new ItemStack[0];
	}

	public TileEntitySolarEvaporationBlock(String fullName)
	{
		super(fullName);

		inventory = new ItemStack[0];
	}

	@Override
	public void onUpdate() 
	{
		if(!worldObj.isRemote && ticker == 5 && !attempted && master == null)
		{
			updateController();
		}
		
		attempted = false;
	}

	public void addToStructure(Coord4D controller)
	{
		master = controller;
	}

	public void controllerGone()
	{
		master = null;
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();

		if(master != null)
		{
			TileEntitySolarEvaporationController tile = getController();
			
			if(tile != null)
			{
				((TileEntitySolarEvaporationController)tile).refresh();
			}
		}
	}

	@Override
	public void onNeighborChange(Block block)
	{
		super.onNeighborChange(block);

		if(!worldObj.isRemote)
		{
			TileEntitySolarEvaporationController tile = getController();
			
			if(tile != null)
			{
				((TileEntitySolarEvaporationController)tile).refresh();
			}
			else {
				updateController();
			}
		}
	}
	
	public void updateController()
	{
		if(!(this instanceof TileEntitySolarEvaporationController))
		{
			TileEntitySolarEvaporationController found = new ControllerFinder().find();
			
			if(found != null)
			{
				found.refresh();
			}
		}
	}
	
	public TileEntitySolarEvaporationController getController()
	{
		if(master != null)
		{
			TileEntity tile = master.getTileEntity(worldObj);
			
			if(tile instanceof TileEntitySolarEvaporationController)
			{
				return (TileEntitySolarEvaporationController)tile;
			}
		}
		
		return null;
	}
	
	public class ControllerFinder
	{
		public TileEntitySolarEvaporationController found;
		
		public Set<Coord4D> iterated = new HashSet<Coord4D>();
		
		public void loop(Coord4D pos)
		{
			if(iterated.size() > 512 || found != null)
			{
				return;
			}
			
			iterated.add(pos);
			
			for(EnumFacing side : EnumFacing.VALUES)
			{
				Coord4D coord = pos.offset(side);
				
				if(!iterated.contains(coord) && coord.getTileEntity(worldObj) instanceof TileEntitySolarEvaporationBlock)
				{
					((TileEntitySolarEvaporationBlock)coord.getTileEntity(worldObj)).attempted = true;
					
					if(coord.getTileEntity(worldObj) instanceof TileEntitySolarEvaporationController)
					{
						found = (TileEntitySolarEvaporationController)coord.getTileEntity(worldObj);
						return;
					}
					
					loop(coord);
				}
			}
		}
		
		public TileEntitySolarEvaporationController find()
		{
			loop(Coord4D.get(TileEntitySolarEvaporationBlock.this));
			
			return found;
		}
	}
}
