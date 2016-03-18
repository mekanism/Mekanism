package mekanism.generators.common.tile.reactor;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.reactor.IFusionReactor;
import mekanism.api.reactor.IReactorBlock;
import mekanism.common.tile.TileEntityElectricBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public abstract class TileEntityReactorBlock extends TileEntityElectricBlock implements IReactorBlock
{
	public IFusionReactor fusionReactor;
	
	public boolean attempted;
	
	public boolean changed;

	public TileEntityReactorBlock()
	{
		super("ReactorBlock", 0);
		inventory = new ItemStack[0];
	}

	public TileEntityReactorBlock(String name, double maxEnergy)
	{
		super(name, maxEnergy);
	}

	@Override
	public void setReactor(IFusionReactor reactor)
	{
		if(reactor != fusionReactor)
		{
			changed = true;
		}
		
		fusionReactor = reactor;
	}

	@Override
	public IFusionReactor getReactor()
	{
		return fusionReactor;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		
		if(getReactor() != null)
		{
			getReactor().formMultiblock(false);
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(changed)
		{
			changed = false;
		}
		
		if(!worldObj.isRemote && ticker == 5 && !attempted && (getReactor() == null || !getReactor().isFormed()))
		{
			updateController();
		}
		
		attempted = false;
	}

	@Override
	public EnumSet<EnumFacing> getOutputtingSides()
	{
		return EnumSet.noneOf(EnumFacing.class);
	}

	@Override
	public EnumSet<EnumFacing> getConsumingSides()
	{
		return EnumSet.noneOf(EnumFacing.class);
	}
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();

		if(!(this instanceof TileEntityReactorController) && getReactor() != null)
		{
			getReactor().formMultiblock(true);
		}
	}
	
	@Override
	public void onAdded()
	{
		super.onAdded();

		if(!worldObj.isRemote)
		{
			if(getReactor() != null)
			{
				getReactor().formMultiblock(false);
			}
			else {
				updateController();
			}
		}
	}
	
	public void updateController()
	{
		if(!(this instanceof TileEntityReactorController))
		{
			TileEntityReactorController found = new ControllerFinder().find();
			
			if(found != null && (found.getReactor() == null || !found.getReactor().isFormed()))
			{
				found.formMultiblock(false);
			}
		}
	}
	
	public class ControllerFinder
	{
		public TileEntityReactorController found;
		
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
				
				if(!iterated.contains(coord) && coord.getTileEntity(worldObj) instanceof TileEntityReactorBlock)
				{
					((TileEntityReactorBlock)coord.getTileEntity(worldObj)).attempted = true;
					
					if(coord.getTileEntity(worldObj) instanceof TileEntityReactorController)
					{
						found = (TileEntityReactorController)coord.getTileEntity(worldObj);
						return;
					}
					
					loop(coord);
				}
			}
		}
		
		public TileEntityReactorController find()
		{
			loop(Coord4D.get(TileEntityReactorBlock.this));
			
			return found;
		}
	}
}
