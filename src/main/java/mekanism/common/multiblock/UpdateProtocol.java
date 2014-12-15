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
import net.minecraft.util.EnumFacing;

public abstract class UpdateProtocol<T>
{
	/** The multiblock nodes that have already been iterated over. */
	public Set<TileEntityMultiblock<T>> iteratedNodes = new HashSet<TileEntityMultiblock<T>>();

	/** The structures found, all connected by some nodes to the pointer. */
	public SynchronizedData<T> structureFound = null;

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
		World worldObj = tile.getWorld();

		int origX = tile.getPos().getX(), origY = tile.getPos().getY(), origZ = tile.getPos().getZ();

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
							else if(isFrame(Coord4D.get(tile).add(x, y, z), origX+xmin, origX+xmax, origY+ymin, origY+ymax, origZ+zmin, origZ+zmax) && !isValidFrame(origX+x, origY+y, origZ+z))
							{
								rightFrame = false;
								break;
							}
							else {
								locations.add(Coord4D.get(tile).add(x, y, z));
							}
						}
						else {
							if(!isAir(origX+x, origY+y, origZ+z))
							{
								isHollow = false;
								break;
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
				SynchronizedData<T> structure = getNewStructure();
				structure.locations = locations;
				structure.volLength = Math.abs(xmax-xmin)+1;
				structure.volHeight = Math.abs(ymax-ymin)+1;
				structure.volWidth = Math.abs(zmax-zmin)+1;
				structure.volume = volume;
				structure.renderLocation = Coord4D.get(tile).add(0, 1, 0);
				
				onStructureCreated(structure, origX, origY, origZ, xmin, xmax, ymin, ymax, zmin, zmax);

				if(structure.locations.contains(Coord4D.get(pointer)) && isCorrectCorner(Coord4D.get(tile), origX+xmin, origY+ymin, origZ+zmin))
				{
					structureFound = structure;
					return;
				}
			}
		}

		iteratedNodes.add(tile);

		for(EnumFacing side : EnumFacing.values())
		{
			TileEntity tileEntity = Coord4D.get(tile).offset(side).getTileEntity(tile.getWorld());

			if(MultiblockManager.areEqual(tileEntity, pointer))
			{
				if(!iteratedNodes.contains(tileEntity))
				{
					loopThrough((TileEntityMultiblock<T>)tileEntity);
				}
			}
		}
	}

	public EnumFacing getSide(Coord4D obj, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
	{
		if(obj.getPos().getX() == xmin)
		{
			return EnumFacing.WEST;
		}
		else if(obj.getPos().getX() == xmax)
		{
			return EnumFacing.EAST;
		}
		else if(obj.getPos().getY() == ymin)
		{
			return EnumFacing.DOWN;
		}
		else if(obj.getPos().getY() == ymax)
		{
			return EnumFacing.UP;
		}
		else if(obj.getPos().getZ() == zmin)
		{
			return EnumFacing.NORTH;
		}
		else if(obj.getPos().getZ() == zmax)
		{
			return EnumFacing.SOUTH;
		}

		return null;
	}

	/**
	 * Whether or not the block at the specified location is an air block.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return
	 */
	private boolean isAir(int x, int y, int z)
	{
		return pointer.getWorld().isAirBlock(x, y, z);
	}

	/**
	 * Whether or not the block at the specified location is a viable node for a dynamic tank.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return
	 */
	private boolean isViableNode(int x, int y, int z)
	{
		TileEntity tile = pointer.getWorld().getTileEntity(new BlockPos(x, y, z));
		
		if(MultiblockManager.areEqual(tile, pointer))
		{
			return true;
		}

		return false;
	}

	/**
	 * If the block at the specified location is on the minimum of all angles of this dynamic tank, and the one to use for the
	 * actual calculation.
	 * @param obj - location to check
	 * @param xmin - minimum x value
	 * @param ymin - minimum y value
	 * @param zmin - minimum z value
	 * @return
	 */
	private boolean isCorrectCorner(Coord4D obj, int xmin, int ymin, int zmin)
	{
		if(obj.getPos().getX() == xmin && obj.getPos().getY() == ymin && obj.getPos().getZ() == zmin)
		{
			return true;
		}

		return false;
	}

	/**
	 * Whether or not the block at the specified location is considered a frame on the dynamic tank.
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
		if(obj.getPos().getX() == xmin && obj.getPos().getY() == ymin)
			return true;
		if(obj.getPos().getX() == xmax && obj.getPos().getY() == ymin)
			return true;
		if(obj.getPos().getX() == xmin && obj.getPos().getY() == ymax)
			return true;
		if(obj.getPos().getX() == xmax && obj.getPos().getY() == ymax)
			return true;

		if(obj.getPos().getX() == xmin && obj.getPos().getZ() == zmin)
			return true;
		if(obj.getPos().getX() == xmax && obj.getPos().getZ() == zmin)
			return true;
		if(obj.getPos().getX() == xmin && obj.getPos().getZ() == zmax)
			return true;
		if(obj.getPos().getX() == xmax && obj.getPos().getZ() == zmax)
			return true;

		if(obj.getPos().getY() == ymin && obj.getPos().getZ() == zmin)
			return true;
		if(obj.getPos().getY() == ymax && obj.getPos().getZ() == zmin)
			return true;
		if(obj.getPos().getY() == ymin && obj.getPos().getZ() == zmax)
			return true;
		if(obj.getPos().getY() == ymax && obj.getPos().getZ() == zmax)
			return true;

		return false;
	}

	/**
	 * Whether or not the block at the specified location serves as a frame for a dynamic tank.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return
	 */
	protected abstract boolean isValidFrame(int x, int y, int z);
	
	protected abstract MultiblockCache<T> getNewCache();
	
	protected abstract SynchronizedData<T> getNewStructure();
	
	protected abstract MultiblockManager<T> getManager();
	
	protected abstract void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<T> cache, MultiblockCache<T> merge);
	
	protected void onFormed() {}
	
	protected void onStructureCreated(SynchronizedData<T> structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {}

	/**
	 * Runs the protocol and updates all tanks that make a part of the dynamic tank.
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
						((TileEntityMultiblock<T>)tileEntity).structure = null;
					}

					return;
				}
			}

			List<Integer> idsFound = new ArrayList<Integer>();
			int idToUse = -1;

			for(Coord4D obj : structureFound.locations)
			{
				TileEntityMultiblock<T> tileEntity = (TileEntityMultiblock<T>)obj.getTileEntity(pointer.getWorld());
				int id = getManager().getInventoryId(tileEntity);

				if(id != -1)
				{
					idsFound.add(id);
				}
			}

			MultiblockCache<T> cache = getNewCache();
			List<ItemStack> rejectedItems = new ArrayList<ItemStack>();

			if(!idsFound.isEmpty())
			{
				for(int id : idsFound)
				{
					if(Mekanism.tankManager.inventories.get(id) != null)
					{
						if(cache == null)
						{
							cache = (MultiblockCache<T>)Mekanism.tankManager.pullInventory(pointer.getWorld(), id);
						}
						else {
							mergeCaches(rejectedItems, cache, (MultiblockCache<T>)getManager().pullInventory(pointer.getWorld(), id));
						}
						
						idToUse = id;
					}
				}
			}
			else {
				idToUse = Mekanism.tankManager.getUniqueInventoryID();
			}
			
			//TODO someday: drop all items in rejectedItems

			cache.apply((T)structureFound);

			onFormed();
			
			structureFound.inventoryID = idToUse;

			for(Coord4D obj : structureFound.locations)
			{
				TileEntityMultiblock<T> tileEntity = (TileEntityMultiblock<T>)obj.getTileEntity(pointer.getWorld());

				tileEntity.structure = (T)structureFound;
			}
		}
		else {
			for(TileEntity tileEntity : iteratedNodes)
			{
				((TileEntityMultiblock<T>)tileEntity).structure = null;
			}
		}
	}
}
