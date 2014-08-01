package mekanism.common.tile;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntitySalinationBlock extends TileEntityContainerBlock
{
	TileEntitySalinationController master;

	public TileEntitySalinationBlock()
	{
		super("SalinationTank");

		inventory = new ItemStack[0];
	}

	public TileEntitySalinationBlock(String fullName)
	{
		super(fullName);

		inventory = new ItemStack[0];
	}

	@Override
	public void onUpdate() {};

	public void addToStructure(TileEntitySalinationController controller)
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
				if(!(this instanceof TileEntitySalinationController))
				{
					TileEntitySalinationController found = new ControllerFinder().find();
					
					if(found != null)
					{
						found.refresh();
					}
				}
			}
		}
	}
	
	public class ControllerFinder
	{
		public TileEntitySalinationController found;
		
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
				
				if(!iterated.contains(coord) && coord.getTileEntity(worldObj) instanceof TileEntitySalinationBlock)
				{
					if(coord.getTileEntity(worldObj) instanceof TileEntitySalinationController)
					{
						found = (TileEntitySalinationController)coord.getTileEntity(worldObj);
						return;
					}
					
					loop(coord);
				}
			}
		}
		
		public TileEntitySalinationController find()
		{
			loop(Coord4D.get(TileEntitySalinationBlock.this));
			
			return found;
		}
	}
}
