package mekanism.common.content.boiler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBasic.BasicType;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntitySuperheatingElement;
import net.minecraft.item.ItemStack;
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
		return BasicType.get(pointer.getWorldObj().getBlock(x, y, z), pointer.getWorldObj().getBlockMetadata(x, y, z)) == BasicType.BOILER_CASING;
	}
	
	@Override
	protected boolean isValidInnerNode(int x, int y, int z)
	{
		if(super.isValidInnerNode(x, y, z))
		{
			return true;
		}

		TileEntity tile = pointer.getWorldObj().getTileEntity(x, y, z);
		
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
				TileEntity tile = coord.getTileEntity(pointer.getWorldObj());
				
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
				System.out.println("No dispersers");
				return false;
			}
			
			//Find a single disperser contained within this multiblock
			Coord4D initDisperser = dispersers.iterator().next();
			
			//Ensure that a full horizontal plane of dispersers exist, surrounding the found disperser
			for(int x = structure.renderLocation.xCoord+1; x < structure.renderLocation.xCoord+structure.volLength-1; x++)
			{
				for(int z = structure.renderLocation.zCoord+1; z < structure.renderLocation.zCoord+structure.volWidth-1; z++)
				{
					TileEntity tile = pointer.getWorldObj().getTileEntity(x, initDisperser.yCoord, z);
					
					if(!(tile instanceof TileEntityPressureDisperser))
					{
						System.out.println("Missing disperser");
						return false;
					}
					
					dispersers.remove(new Coord4D(x, initDisperser.yCoord, z, pointer.getWorldObj().provider.dimensionId));
				}
			}
			
			//If there are more dispersers than those on the plane found, the structure is invalid
			if(dispersers.size() > 0)
			{
				System.out.println("Bad disperser");
				return false;
			}
			
			if(elements.size() > 0)
			{
				structure.superheatingElements = new NodeCounter(new NodeChecker() {
					@Override
					public boolean isValid(Coord4D coord) 
					{
						return coord.getTileEntity(pointer.getWorldObj()) instanceof TileEntitySuperheatingElement;
					}
				}).calculate(elements.iterator().next());
			}
			
			if(elements.size() > structure.superheatingElements)
			{
				System.out.println("Disconnected elements");
				return false;
			}
			
			Coord4D initAir = null;
			int totalAir = 0;
			
			//Find the first available block in the structure for water storage (including casings)
			for(int x = structure.renderLocation.xCoord; x < structure.renderLocation.xCoord+structure.volLength; x++)
			{
				for(int y = structure.renderLocation.yCoord; y < initDisperser.yCoord; y++)
				{
					for(int z = structure.renderLocation.zCoord; z < structure.renderLocation.zCoord+structure.volWidth; z++)
					{
						if(pointer.getWorldObj().isAirBlock(x, y, z) || isViableNode(x, y, z))
						{
							initAir = new Coord4D(x, y, z, pointer.getWorldObj().provider.dimensionId);
							totalAir++;
						}
					}
				}
			}
			
			//Some air must exist for the structure to be valid
			if(initAir == null)
			{
				System.out.println("No air");
				return false;
			}
			
			final int total = totalAir;
			
			structure.waterVolume = new NodeCounter(new NodeChecker() {
				@Override
				public boolean isValid(Coord4D coord) 
				{
					return coord.yCoord >= structure.renderLocation.yCoord-1 && coord.yCoord < initDisperser.yCoord && 
							coord.xCoord >= structure.renderLocation.xCoord && coord.xCoord < structure.renderLocation.xCoord+structure.volLength && 
							coord.zCoord >= structure.renderLocation.zCoord && coord.zCoord < structure.renderLocation.zCoord+structure.volWidth &&
							(coord.isAirBlock(pointer.getWorldObj()) || isViableNode(coord.xCoord, coord.yCoord, coord.zCoord));
				}
			}).calculate(initAir);
			
			//Make sure all air blocks are connected
			if(totalAir > structure.waterVolume)
			{
				System.out.println("nonconnected air");
				return false;
			}
			
			int steamHeight = (structure.renderLocation.yCoord+structure.volHeight-2)-initDisperser.yCoord;
			structure.steamVolume = structure.volWidth*structure.volLength*steamHeight;
			
			structure.upperRenderLocation = new Coord4D(structure.renderLocation.xCoord, initDisperser.yCoord+1, structure.renderLocation.zCoord);
			System.out.println(structure.superheatingElements + " " + structure.waterVolume + " " + structure.steamVolume);
			
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
		
		((BoilerCache)cache).temperature = Math.max(((BoilerCache)cache).temperature, ((BoilerCache)merge).temperature);
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
			if(obj.getTileEntity(pointer.getWorldObj()) instanceof TileEntityBoilerValve)
			{
				ValveData data = new ValveData();
				data.location = obj;
				data.side = getSide(obj, origX+xmin, origX+xmax, origY+ymin, origY+ymax, origZ+zmin, origZ+zmax);

				structure.valves.add(data);
			}
		}
	}
}
