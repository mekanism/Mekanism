package mekanism.common.content.boiler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.util.StackUtils;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.content.boiler.SynchronizedBoilerData.ValveData;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntitySuperheatingElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.tileentity.TileEntity;

public class BoilerUpdateProtocol extends UpdateProtocol<SynchronizedBoilerData>
{
	public static final int WATER_PER_TANK = 16000;
	public static final int STEAM_PER_TANK = 160000;

	public BoilerUpdateProtocol(TileEntityBoilerCasing tileEntity)
	{
		super(tileEntity);
	}

	@Override
	protected boolean isValidFrame(int x, int y, int z)
	{
		return BasicBlockType.get(pointer.getWorld().getBlockState(new BlockPos(x, y, z))) == BasicBlockType.BOILER_CASING;
	}
	
	@Override
	protected boolean isValidInnerNode(int x, int y, int z)
	{
		if(super.isValidInnerNode(x, y, z))
		{
			return true;
		}

		TileEntity tile = pointer.getWorld().getTileEntity(new BlockPos(x, y, z));
		
		return tile instanceof TileEntityPressureDisperser || tile instanceof TileEntitySuperheatingElement;
	}
	
	@Override
	protected boolean canForm(SynchronizedBoilerData structure)
	{
		if(structure.volHeight >= 3)
		{
			Set<Coord4D> dispersers = new HashSet<Coord4D>();
			Set<Coord4D> elements = new HashSet<Coord4D>();
			
			for(Coord4D coord : innerNodes)
			{
				TileEntity tile = coord.getTileEntity(pointer.getWorld());
				
				if(tile instanceof TileEntityPressureDisperser)
				{
					dispersers.add(coord);
				}
				else if(tile instanceof TileEntitySuperheatingElement)
				{
					elements.add(coord);
				}
			}
			
			int prevDispersers = dispersers.size();
			
			//Ensure at least one disperser exists
			if(dispersers.size() == 0)
			{
				return false;
			}
			
			//Find a single disperser contained within this multiblock
			Coord4D initDisperser = dispersers.iterator().next();
			
			//Ensure that a full horizontal plane of dispersers exist, surrounding the found disperser
			Coord4D pos = new Coord4D(structure.renderLocation.getX(), initDisperser.getY(), structure.renderLocation.getZ(), pointer.getWorld().provider.getDimensionId());
			for(int x = 1; x < structure.volLength-1; x++)
			{
				for(int z = 1; z < structure.volWidth-1; z++)
				{
					Coord4D coord4D = pos.add(x, 0, z);
					TileEntity tile = pointer.getWorld().getTileEntity(coord4D);
					
					if(!(tile instanceof TileEntityPressureDisperser))
					{
						return false;
					}
					
					dispersers.remove(coord4D);
				}
			}
			
			//If there are more dispersers than those on the plane found, the structure is invalid
			if(dispersers.size() > 0)
			{
				return false;
			}
			
			structure.superheatingElements = new NodeCounter(new NodeChecker() {
				@Override
				public boolean isValid(Coord4D coord) 
				{
					return coord.getTileEntity(pointer.getWorld()) instanceof TileEntitySuperheatingElement;
				}
			}).calculate(elements.iterator().next());
			
			if(elements.size() > structure.superheatingElements)
			{
				return false;
			}
			
			Coord4D initAir = null;
			int totalAir = 0;

			pos = new Coord4D(structure.renderLocation, pointer.getWorld().provider.getDimensionId());
			//Find the first available block in the structure for water storage (including casings)
			for(int x = 0; x < structure.volLength; x++)
			{
				for(int y = 0; y < initDisperser.getY() - structure.renderLocation.getY(); y++)
				{
					for(int z = 0; z < structure.volWidth; z++)
					{
						if(pointer.getWorld().isAirBlock(pos.add(x, y, z)) || isViableNode(pos.add(x, y, z)))
						{
							initAir = new Coord4D(x, y, z, pointer.getWorld().provider.getDimensionId());
							totalAir++;
						}
					}
				}
			}
			
			//Some air must exist for the structure to be valid
			if(initAir == null)
			{
				return false;
			}
			
			structure.waterVolume = new NodeCounter(new NodeChecker() {
				@Override
				public boolean isValid(Coord4D coord) 
				{
					return coord.getY() < initDisperser.getY() && (coord.isAirBlock(pointer.getWorld()) || isViableNode(coord));
				}
			}).calculate(initAir);
			
			//Make sure all air blocks are connected
			if(totalAir > structure.waterVolume)
			{
				return false;
			}
			
			int steamHeight = (structure.renderLocation.getY()+structure.volHeight)-initDisperser.getY();
			structure.steamVolume = structure.volWidth*structure.volLength*steamHeight;
			
			return true;
		}
		
		return false;
	}

	@Override
	protected BoilerCache getNewCache()
	{
		return new BoilerCache();
	}

	@Override
	protected SynchronizedBoilerData getNewStructure()
	{
		return new SynchronizedBoilerData();
	}

	@Override
	protected MultiblockManager<SynchronizedBoilerData> getManager()
	{
		return Mekanism.boilerManager;
	}

	@Override
	protected void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<SynchronizedBoilerData> cache, MultiblockCache<SynchronizedBoilerData> merge)
	{
		if(((BoilerCache)cache).water == null)
		{
			((BoilerCache)cache).water = ((BoilerCache)merge).water;
		}
		else if(((BoilerCache)merge).water != null && ((BoilerCache)cache).water.isFluidEqual(((BoilerCache)merge).water))
		{
			((BoilerCache)cache).water.amount += ((BoilerCache)merge).water.amount;
		}

		if(((BoilerCache)cache).steam == null)
		{
			((BoilerCache)cache).steam = ((BoilerCache)merge).steam;
		}
		else if(((BoilerCache)merge).steam != null && ((BoilerCache)cache).steam.isFluidEqual(((BoilerCache)merge).steam))
		{
			((BoilerCache)cache).steam.amount += ((BoilerCache)merge).steam.amount;
		}

		List<ItemStack> rejects = StackUtils.getMergeRejects(((BoilerCache)cache).inventory, ((BoilerCache)merge).inventory);

		if(!rejects.isEmpty())
		{
			rejectedItems.addAll(rejects);
		}

		StackUtils.merge(((BoilerCache)cache).inventory, ((BoilerCache)merge).inventory);
	}

	@Override
	protected void onFormed()
	{
		if((structureFound).waterStored != null)
		{
			(structureFound).waterStored.amount = Math.min((structureFound).waterStored.amount, structureFound.waterVolume*WATER_PER_TANK);
		}
		
		if((structureFound).steamStored != null)
		{
			(structureFound).steamStored.amount = Math.min((structureFound).steamStored.amount, structureFound.steamVolume*STEAM_PER_TANK);
		}
	}

	@Override
	protected void onStructureCreated(SynchronizedBoilerData structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
	{
		for(Coord4D obj : structure.locations)
		{
			if(obj.getTileEntity(pointer.getWorld()) instanceof TileEntityBoilerValve)
			{
				ValveData data = new ValveData();
				data.location = obj;
				data.side = getSide(obj, origX+xmin, origX+xmax, origY+ymin, origY+ymax, origZ+zmin, origZ+zmax);

				structure.valves.add(data);
			}
		}
	}
}
