package mekanism.common.tile;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntitySolarEvaporationBlock extends TileEntityContainerBlock
{
	public TileEntitySolarEvaporationController master;
	
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

	public void addToStructure(TileEntitySolarEvaporationController controller)
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
			master.refresh();
		}
	}

	@Override
	public void onNeighborChange(Block block)
	{
		super.onNeighborChange(block);

		if(!worldObj.isRemote)
		{
			if(master != null)
			{
				master.refresh();
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
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				Coord4D coord = pos.getFromSide(side);
				
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
