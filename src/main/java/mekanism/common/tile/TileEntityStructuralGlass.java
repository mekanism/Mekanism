package mekanism.common.tile;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityStructuralGlass extends TileEntity implements IStructuralMultiblock
{
	public Coord4D master;
	
	@Override
	public boolean onActivate(EntityPlayer player) 
	{
		if(master != null)
		{
			if(master.getTileEntity(worldObj) instanceof IMultiblock)
			{
				return ((IMultiblock)master.getTileEntity(worldObj)).onActivate(player);
			}
			else {
				master = null;
			}
		}
		
		return false;
	}
	
	@Override
	public void update()
	{
		if(master != null)
		{
			if(master.getTileEntity(worldObj) instanceof IMultiblock)
			{
				((IMultiblock)master.getTileEntity(worldObj)).update();
			}
			else {
				master = null;
			}
		}
		else {
			IMultiblock multiblock = new ControllerFinder().find();
			
			if(multiblock != null)
			{
				multiblock.update();
			}
		}
	}

	@Override
	public boolean canInterface(TileEntity controller) 
	{
		return true;
	}
	
	@Override
	public void setController(Coord4D coord)
	{
		master = coord;
	}
	
	public class ControllerFinder
	{
		public IMultiblock found;
		
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
				TileEntity tile = coord.getTileEntity(worldObj);
				
				if(!iterated.contains(coord))
				{
					if(tile instanceof IMultiblock)
					{
						found = (IMultiblock)coord.getTileEntity(worldObj);
						return;
					}
					else if(tile instanceof IStructuralMultiblock)
					{
						loop(coord);
					}
				}
			}
		}
		
		public IMultiblock find()
		{
			loop(Coord4D.get(TileEntityStructuralGlass.this));
			
			return found;
		}
	}
}
