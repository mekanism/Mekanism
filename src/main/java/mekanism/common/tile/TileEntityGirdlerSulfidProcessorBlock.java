package mekanism.common.tile;

import mekanism.api.Coord4D;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashSet;
import java.util.Set;

public class TileEntityGirdlerSulfidProcessorBlock extends TileEntityContainerBlock
{
	public Coord4D master;

	public boolean attempted;

	public TileEntityGirdlerSulfidProcessorBlock()
	{
		super("GirdlerSulfidProcessorBlock");

		inventory = new ItemStack[0];
	}

	public TileEntityGirdlerSulfidProcessorBlock(String fullName)
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
			TileEntityGirdlerSulfidProcessorController tile = getController();
			
			if(tile != null)
			{
				((TileEntityGirdlerSulfidProcessorController)tile).refresh();
			}
		}
	}

	@Override
	public void onNeighborChange(Block block)
	{
		super.onNeighborChange(block);

		if(!worldObj.isRemote)
		{
			TileEntityGirdlerSulfidProcessorController tile = getController();
			
			if(tile != null)
			{
				((TileEntityGirdlerSulfidProcessorController)tile).refresh();
			}
			else {
				updateController();
			}
		}
	}
	
	public void updateController()
	{
		if(!(this instanceof TileEntityGirdlerSulfidProcessorController))
		{
			TileEntityGirdlerSulfidProcessorController found = new ControllerFinder().find();
			
			if(found != null)
			{
				found.refresh();
			}
		}
	}
	
	public TileEntityGirdlerSulfidProcessorController getController()
	{
		if(master != null)
		{
			TileEntity tile = master.getTileEntity(worldObj);
			
			if(tile instanceof TileEntityGirdlerSulfidProcessorController)
			{
				return (TileEntityGirdlerSulfidProcessorController)tile;
			}
		}
		
		return null;
	}
	
	public class ControllerFinder
	{
		public TileEntityGirdlerSulfidProcessorController found;
		
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
				
				if(!iterated.contains(coord) && coord.getTileEntity(worldObj) instanceof TileEntityGirdlerSulfidProcessorBlock)
				{
					((TileEntityGirdlerSulfidProcessorBlock)coord.getTileEntity(worldObj)).attempted = true;
					
					if(coord.getTileEntity(worldObj) instanceof TileEntityGirdlerSulfidProcessorController)
					{
						found = (TileEntityGirdlerSulfidProcessorController)coord.getTileEntity(worldObj);
						return;
					}
					
					loop(coord);
				}
			}
		}
		
		public TileEntityGirdlerSulfidProcessorController find()
		{
			loop(Coord4D.get(TileEntityGirdlerSulfidProcessorBlock.this));
			
			return found;
		}
	}
}
