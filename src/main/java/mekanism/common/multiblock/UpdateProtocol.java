package mekanism.common.multiblock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityMultiblock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class UpdateProtocol<T extends SynchronizedData<T>>
{
	/** The multiblock nodes that have already been iterated over. */
	public Set<TileEntityMultiblock<T>> iteratedNodes = new HashSet<TileEntityMultiblock<T>>();
	
	public Set<Coord4D> innerNodes = new HashSet<Coord4D>();

	/** The structures found, all connected by some nodes to the pointer. */
	public T structureFound = null;

	/** The original block the calculation is getting run from. */
	public TileEntityMultiblock<T> pointer;

	public UpdateProtocol(TileEntityMultiblock<T> tileEntity)
	{
		pointer = tileEntity;
	}

	/**
	 * Recursively loops through each node connected to the given TileEntity.
	 * @param tile - the TileEntity to loop over
	 */
	public void loopThrough(TileEntityMultiblock<T> tile)
	{
		World worldObj = tile.getWorldObj();

		int origX = tile.xCoord, origY = tile.yCoord, origZ = tile.zCoord;

		boolean isCorner = true;
		boolean isHollow = true;
		boolean rightBlocks = true;
		boolean rightFrame = true;

		Set<Coord4D> locations = new HashSet<Coord4D>();

		int xmin = 0, xmax = 0, ymin = 0, ymax = 0, zmin = 0, zmax = 0;

		int x = 0, y = 0, z = 0;

		int volume = 0;

		if((isViableNode(origX + 1, origY, origZ) && isViableNode(origX - 1, origY, origZ)) ||
				(isViableNode(origX, origY + 1, origZ) && isViableNode(origX, origY - 1, origZ)) ||
				(isViableNode(origX, origY, origZ + 1) && isViableNode(origX, origY, origZ - 1)))
		{
			isCorner = false;
		}

		if(isCorner)
		{
			if(isViableNode(origX+1, origY, origZ))
			{
				xmin = 0;

				while(isViableNode(origX+x+1, origY, origZ))
				{
					x++;
				}

				xmax = x;
			}
			else {
				xmax = 0;

				while(isViableNode(origX+x-1, origY, origZ))
				{
					x--;
				}

				xmin = x;
			}

			if(isViableNode(origX, origY+1, origZ))
			{
				ymin = 0;

				while(isViableNode(origX, origY+y+1, origZ))
				{
					y++;
				}

				ymax = y;
			}
			else {
				ymax = 0;

				while(isViableNode(origX, origY+y-1 ,origZ))
				{
					y--;
				}

				ymin = y;
			}

			if(isViableNode(origX, origY, origZ+1))
			{
				zmin = 0;

				while(isViableNode(origX, origY, origZ+z+1))
				{
					z++;
				}

				zmax = z;
			}
			else {
				zmax = 0;

				while(isViableNode(origX, origY, origZ+z-1))
				{
					z--;
				}

				zmin = z;
			}

			for(x = xmin; x <= xmax; x++)
			{
				for(y = ymin; y <= ymax; y++)
				{
					for(z = zmin; z <= zmax; z++)
					{
						if(x == xmin || x == xmax || y == ymin || y == ymax || z == zmin || z == zmax)
						{
							if(!isViableNode(origX+x, origY+y, origZ+z))
							{
								rightBlocks = false;
								break;
							}
							else if(isFrame(Coord4D.get(tile).translate(x, y, z), origX+xmin, origX+xmax, origY+ymin, origY+ymax, origZ+zmin, origZ+zmax) && !isValidFrame(origX+x, origY+y, origZ+z))
							{
								rightFrame = false;
								break;
							}
							else {
								locations.add(Coord4D.get(tile).translate(x, y, z));
							}
						}
						else {
							if(!isValidInnerNode(origX+x, origY+y, origZ+z))
							{
								isHollow = false;
								break;
							}
							else {
								innerNodes.add(new Coord4D(origX+x, origY+y, origZ+z, pointer.getWorldObj().provider.dimensionId));
							}

							volume++;
						}
					}
					
					if(!isHollow || !rightBlocks || !rightFrame)
					{
						break;
					}
				}
				
				if(!isHollow || !rightBlocks || !rightFrame)
				{
					break;
				}
			}
		}

		volume += locations.size();

		if(volume >= 27 && volume <= 5832 && locations.size() >= 26)
		{
			if(rightBlocks && rightFrame && isHollow && isCorner)
			{
				T structure = getNewStructure();
				structure.locations = locations;
				structure.volLength = Math.abs(xmax-xmin)+1;
				structure.volHeight = Math.abs(ymax-ymin)+1;
				structure.volWidth = Math.abs(zmax-zmin)+1;
				structure.volume = volume;
				structure.renderLocation = Coord4D.get(tile).translate(0, 1, 0);
				structure.minLocation = Coord4D.get(tile).translate(xmin, ymin, zmin);
				structure.maxLocation = Coord4D.get(tile).translate(xmax, ymax, zmax);
				
				if(structure.volLength >= 3 && structure.volHeight >= 3 && structure.volWidth >= 3)
				{
					onStructureCreated(structure, origX, origY, origZ, xmin, xmax, ymin, ymax, zmin, zmax);
	
					if(structure.locations.contains(Coord4D.get(pointer)) && isCorrectCorner(Coord4D.get(tile), origX+xmin, origY+ymin, origZ+zmin))
					{
						structureFound = structure;
						return;
					}
				}
			}
		}

		iteratedNodes.add(tile);

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = Coord4D.get(tile).getFromSide(side).getTileEntity(tile.getWorldObj());

			if(MultiblockManager.areEqual(tileEntity, pointer))
			{
				if(!iteratedNodes.contains(tileEntity))
				{
					loopThrough((TileEntityMultiblock<T>)tileEntity);
				}
			}
		}
	}

	public ForgeDirection getSide(Coord4D obj, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
	{
		if(obj.xCoord == xmin)
		{
			return ForgeDirection.WEST;
		}
		else if(obj.xCoord == xmax)
		{
			return ForgeDirection.EAST;
		}
		else if(obj.yCoord == ymin)
		{
			return ForgeDirection.DOWN;
		}
		else if(obj.yCoord == ymax)
		{
			return ForgeDirection.UP;
		}
		else if(obj.zCoord == zmin)
		{
			return ForgeDirection.NORTH;
		}
		else if(obj.zCoord == zmax)
		{
			return ForgeDirection.SOUTH;
		}

		return ForgeDirection.UNKNOWN;
	}

	/**
	 * Whether or not the block at the specified location is an air block.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return
	 */
	protected boolean isAir(int x, int y, int z)
	{
		return pointer.getWorldObj().isAirBlock(x, y, z);
	}
	
	protected boolean isValidInnerNode(int x, int y, int z)
	{
		return isAir(x, y, z);
	}

	/**
	 * Whether or not the block at the specified location is a viable node for a multiblock structure.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return
	 */
	private boolean isViableNode(int x, int y, int z)
	{
		TileEntity tile = pointer.getWorldObj().getTileEntity(x, y, z);
		
		if(MultiblockManager.areEqual(tile, pointer))
		{
			return true;
		}

		return false;
	}

	/**
	 * If the block at the specified location is on the minimum of all angles of this multiblock structure, and the one to use for the
	 * actual calculation.
	 * @param obj - location to check
	 * @param xmin - minimum x value
	 * @param ymin - minimum y value
	 * @param zmin - minimum z value
	 * @return
	 */
	private boolean isCorrectCorner(Coord4D obj, int xmin, int ymin, int zmin)
	{
		if(obj.xCoord == xmin && obj.yCoord == ymin && obj.zCoord == zmin)
		{
			return true;
		}

		return false;
	}

	/**
	 * Whether or not the block at the specified location is considered a frame on the multiblock structure.
	 * @param obj - location to check
	 * @param xmin - minimum x value
	 * @param xmax - maximum x value
	 * @param ymin - minimum y value
	 * @param ymax - maximum y value
	 * @param zmin - minimum z value
	 * @param zmax - maximum z value
	 * @return
	 */
	private boolean isFrame(Coord4D obj, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
	{
		if(obj.xCoord == xmin && obj.yCoord == ymin)
			return true;
		if(obj.xCoord == xmax && obj.yCoord == ymin)
			return true;
		if(obj.xCoord == xmin && obj.yCoord == ymax)
			return true;
		if(obj.xCoord == xmax && obj.yCoord == ymax)
			return true;

		if(obj.xCoord == xmin && obj.zCoord == zmin)
			return true;
		if(obj.xCoord == xmax && obj.zCoord == zmin)
			return true;
		if(obj.xCoord == xmin && obj.zCoord == zmax)
			return true;
		if(obj.xCoord == xmax && obj.zCoord == zmax)
			return true;

		if(obj.yCoord == ymin && obj.zCoord == zmin)
			return true;
		if(obj.yCoord == ymax && obj.zCoord == zmin)
			return true;
		if(obj.yCoord == ymin && obj.zCoord == zmax)
			return true;
		if(obj.yCoord == ymax && obj.zCoord == zmax)
			return true;

		return false;
	}

	/**
	 * Whether or not the block at the specified location serves as a frame for a multiblock structure.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return
	 */
	protected abstract boolean isValidFrame(int x, int y, int z);
	
	protected abstract MultiblockCache<T> getNewCache();
	
	protected abstract T getNewStructure();
	
	protected abstract MultiblockManager<T> getManager();
	
	protected abstract void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<T> cache, MultiblockCache<T> merge);
	
	protected void onFormed() {}
	
	protected void onStructureCreated(T structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {}
	
	public void onStructureDestroyed(T structure) {}

	/**
	 * Runs the protocol and updates all nodes that make a part of the multiblock.
	 */
	public void doUpdate()
	{
		loopThrough(pointer);

		if(structureFound != null)
		{
			for(TileEntityMultiblock<T> tileEntity : iteratedNodes)
			{
				if(!structureFound.locations.contains(Coord4D.get(tileEntity)))
				{
					for(TileEntity tile : iteratedNodes)
					{
						((TileEntityMultiblock<T>)tile).structure = null;
					}

					return;
				}
			}

			List<Integer> idsFound = new ArrayList<Integer>();
			int idToUse = -1;

			for(Coord4D obj : structureFound.locations)
			{
				TileEntityMultiblock<T> tileEntity = (TileEntityMultiblock<T>)obj.getTileEntity(pointer.getWorldObj());

				if(tileEntity.cachedID != -1)
				{
					idsFound.add(tileEntity.cachedID);
				}
			}

			MultiblockCache<T> cache = getNewCache();
			List<ItemStack> rejectedItems = new ArrayList<ItemStack>();

			if(!idsFound.isEmpty())
			{
				for(int id : idsFound)
				{
					if(getManager().inventories.get(id) != null)
					{
						if(cache == null)
						{
							cache = getManager().pullInventory(pointer.getWorldObj(), id);
						}
						else {
							mergeCaches(rejectedItems, cache, getManager().pullInventory(pointer.getWorldObj(), id));
						}
						
						idToUse = id;
					}
				}
			}
			else {
				idToUse = getManager().getUniqueInventoryID();
			}
			
			//TODO someday: drop all items in rejectedItems
			//TODO seriously this needs to happen soon
			//TODO perhaps drop from pointer?

			cache.apply((T)structureFound);

			onFormed();
			
			structureFound.inventoryID = idToUse;

			for(Coord4D obj : structureFound.locations)
			{
				TileEntityMultiblock<T> tileEntity = (TileEntityMultiblock<T>)obj.getTileEntity(pointer.getWorldObj());

				tileEntity.structure = (T)structureFound;
			}
		}
		else {
			for(TileEntityMultiblock<T> tileEntity : iteratedNodes)
			{
				if(tileEntity.structure != null && !tileEntity.structure.destroyed)
				{
					onStructureDestroyed(tileEntity.structure);
					tileEntity.structure.destroyed = true;
				}
				
				tileEntity.structure = null;
			}
		}
	}
}
