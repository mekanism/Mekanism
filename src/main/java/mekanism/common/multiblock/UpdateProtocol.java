package mekanism.common.multiblock;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.tile.TileEntityMultiblock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class UpdateProtocol<T extends SynchronizedData<T>>
{
	/** The multiblock nodes that have already been iterated over. */
	public Set<Coord4D> iteratedNodes = new HashSet<>();
	
	public Set<Coord4D> innerNodes = new HashSet<>();

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
	 * @param coord - the TileEntity coords to loop over
	 */
	public void loopThrough(Coord4D coord, Deque<Coord4D> queue)
	{
		int origX = coord.x, origY = coord.y, origZ = coord.z;

		boolean isCorner = true;
		boolean isHollow = true;
		boolean rightBlocks = true;
		boolean rightFrame = true;

		Set<Coord4D> locations = new HashSet<>();

		int xmin = 0, xmax = 0, ymin = 0, ymax = 0, zmin = 0, zmax = 0;

		int x = 0, y = 0, z = 0;

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
							else if(isFrame(coord.translate(x, y, z), origX+xmin, origX+xmax, origY+ymin, origY+ymax, origZ+zmin, origZ+zmax) && !isValidFrame(origX+x, origY+y, origZ+z))
							{
								rightFrame = false;
								break;
							}
							else {
								locations.add(coord.translate(x, y, z));
							}
						}
						else {
							if(!isValidInnerNode(origX+x, origY+y, origZ+z))
							{
								isHollow = false;
								break;
							}
							else {
								if(!isAir(origX+x, origY+y, origZ+z))
								{
									innerNodes.add(new Coord4D(origX+x, origY+y, origZ+z, pointer.getWorld().provider.getDimension()));
								}
							}
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

		if(Math.abs(xmax-xmin)+1 <= 18 && Math.abs(ymax-ymin)+1 <= 18 && Math.abs(zmax-zmin)+1 <= 18)
		{
			if(rightBlocks && rightFrame && isHollow && isCorner)
			{
				T structure = getNewStructure();
				structure.locations = locations;
				structure.volLength = Math.abs(xmax-xmin)+1;
				structure.volHeight = Math.abs(ymax-ymin)+1;
				structure.volWidth = Math.abs(zmax-zmin)+1;
				structure.volume = structure.volLength*structure.volHeight*structure.volWidth;
				structure.renderLocation = coord.translate(0, 1, 0);
				structure.minLocation = coord.translate(xmin, ymin, zmin);
				structure.maxLocation = coord.translate(xmax, ymax, zmax);
				
				if(structure.volLength >= 3 && structure.volHeight >= 3 && structure.volWidth >= 3)
				{
					onStructureCreated(structure, origX, origY, origZ, xmin, xmax, ymin, ymax, zmin, zmax);
	
					if(structure.locations.contains(Coord4D.get(pointer)) && isCorrectCorner(coord, origX+xmin, origY+ymin, origZ+zmin))
					{
						if(canForm(structure))
						{
							structureFound = structure;
							return;
						}
					}
				}
			}
		}

		innerNodes.clear();
		iteratedNodes.add(coord);
		
		if(iteratedNodes.size() > 2048)
		{
			return;
		}

		for(EnumFacing side : EnumFacing.VALUES)
		{
			Coord4D sideCoord = coord.offset(side);
			
			if(isViableNode(sideCoord.getPos()))
			{
				if(!iteratedNodes.contains(sideCoord))
				{
					queue.addLast(sideCoord);
				}
			}
		}
	}
	
	protected boolean canForm(T structure)
	{
		return true;
	}

	public EnumFacing getSide(Coord4D obj, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
	{
		if(obj.x == xmin)
		{
			return EnumFacing.WEST;
		}
		else if(obj.x == xmax)
		{
			return EnumFacing.EAST;
		}
		else if(obj.y == ymin)
		{
			return EnumFacing.DOWN;
		}
		else if(obj.y == ymax)
		{
			return EnumFacing.UP;
		}
		else if(obj.z == zmin)
		{
			return EnumFacing.NORTH;
		}
		else if(obj.z == zmax)
		{
			return EnumFacing.SOUTH;
		}

		return null;
	}

	/**
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return Whether or not the block at the specified location is an air block.
	 */
	protected boolean isAir(int x, int y, int z)
	{
		return pointer.getWorld().isAirBlock(new BlockPos(x, y, z));
	}
	
	protected boolean isValidInnerNode(int x, int y, int z)
	{
		return isAir(x, y, z);
	}

	/**
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return Whether or not the block at the specified location is a viable node for a multiblock structure.
	 */
	public boolean isViableNode(int x, int y, int z)
	{
		TileEntity tile = new Coord4D(x, y, z, pointer.getWorld().provider.getDimension()).getTileEntity(pointer.getWorld());

		if(tile instanceof IStructuralMultiblock)
		{
			if(((IStructuralMultiblock)tile).canInterface(pointer))
			{
				return true;
			}
		}

		if(MultiblockManager.areEqual(tile, pointer))
		{
			return true;
		}

		return false;
	}

	/**
	 * @param pos - coordinates
	 * @return Whether or not the block at the specified location is a viable node for a multiblock structure.
	 */
	public boolean isViableNode(BlockPos pos)
	{
		TileEntity tile = new Coord4D(pos, pointer.getWorld()).getTileEntity(pointer.getWorld());
		
		if(tile == null || !tile.hasWorld() || tile.isInvalid())
		{
			return false;
		}

		if(tile instanceof IStructuralMultiblock)
		{
			if(((IStructuralMultiblock)tile).canInterface(pointer))
			{
				return true;
			}
		}

		if(MultiblockManager.areEqual(tile, pointer))
		{
			return true;
		}

		return false;
	}

	/**
	 *
	 * @param obj - location to check
	 * @param xmin - minimum x value
	 * @param ymin - minimum y value
	 * @param zmin - minimum z value
	 * @return If the block at the specified location is on the minimum of all angles of this multiblock structure, and the one to use for the
	 * actual calculation.
	 */
	private boolean isCorrectCorner(Coord4D obj, int xmin, int ymin, int zmin)
	{
		if(obj.x == xmin && obj.y == ymin && obj.z == zmin)
		{
			return true;
		}

		return false;
	}

	/**
	 * @param obj - location to check
	 * @param xmin - minimum x value
	 * @param xmax - maximum x value
	 * @param ymin - minimum y value
	 * @param ymax - maximum y value
	 * @param zmin - minimum z value
	 * @param zmax - maximum z value
	 * @return Whether or not the block at the specified location is considered a frame on the multiblock structure.
	 */
	private boolean isFrame(Coord4D obj, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
	{
		if(obj.x == xmin && obj.y == ymin)
			return true;
		if(obj.x == xmax && obj.y == ymin)
			return true;
		if(obj.x == xmin && obj.y == ymax)
			return true;
		if(obj.x == xmax && obj.y == ymax)
			return true;

		if(obj.x == xmin && obj.z == zmin)
			return true;
		if(obj.x == xmax && obj.z == zmin)
			return true;
		if(obj.x == xmin && obj.z == zmax)
			return true;
		if(obj.x == xmax && obj.z == zmax)
			return true;

		if(obj.y == ymin && obj.z == zmin)
			return true;
		if(obj.y == ymax && obj.z == zmin)
			return true;
		if(obj.y == ymin && obj.z == zmax)
			return true;
		if(obj.y == ymax && obj.z == zmax)
			return true;

		return false;
	}

	/**
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return Whether or not the block at the specified location serves as a frame for a multiblock structure.
	 */
	protected abstract boolean isValidFrame(int x, int y, int z);
	
	protected abstract MultiblockCache<T> getNewCache();
	
	protected abstract T getNewStructure();
	
	protected abstract MultiblockManager<T> getManager();
	
	protected abstract void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<T> cache, MultiblockCache<T> merge);
	
	protected void onFormed() 
	{
		for(Coord4D coord : structureFound.internalLocations)
		{
			TileEntity tile = coord.getTileEntity(pointer.getWorld());
			
			if(tile instanceof TileEntityInternalMultiblock)
			{
				((TileEntityInternalMultiblock)tile).setMultiblock(structureFound.inventoryID);
			}
		}
	}
	
	protected void onStructureCreated(T structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {}
	
	public void onStructureDestroyed(T structure) 
	{
		for(Coord4D coord : structure.internalLocations)
		{
			TileEntity tile = coord.getTileEntity(pointer.getWorld());
			
			if(tile instanceof TileEntityInternalMultiblock)
			{
				((TileEntityInternalMultiblock)tile).setMultiblock(null);
			}
		}
	}
	
	public void killInnerNode(Coord4D coord)
	{
		TileEntity tile = coord.getTileEntity(pointer.getWorld());
		
		if(tile instanceof TileEntityInternalMultiblock)
		{
			((TileEntityInternalMultiblock)tile).setMultiblock(null);
		}
	}

	/**
	 * Runs the protocol and updates all nodes that make a part of the multiblock.
	 */
	public void doUpdate()
	{
		Deque<Coord4D> pathingQueue = new LinkedList<>();
		pathingQueue.add(Coord4D.get(pointer));
		while (pathingQueue.peek() != null) {
			Coord4D next = pathingQueue.removeFirst();
			if (!iteratedNodes.contains(next)) {
				loopThrough(next, pathingQueue);
			}
		}
		
		if(structureFound != null)
		{
			for(Coord4D coord : iteratedNodes)
			{
				if(!structureFound.locations.contains(coord))
				{
					for(Coord4D newCoord : iteratedNodes)
					{
						TileEntity tile = newCoord.getTileEntity(pointer.getWorld());
						
						if(tile instanceof TileEntityMultiblock)
						{
							((TileEntityMultiblock<?>)tile).structure = null;
						}
						else if(tile instanceof IStructuralMultiblock)
						{
							((IStructuralMultiblock)tile).setController(null);
						}
					}
					
					for(Coord4D newCoord : innerNodes)
					{
						killInnerNode(newCoord);
					}

					return;
				}
			}

			List<String> idsFound = new ArrayList<>();
			String idToUse = null;

			for(Coord4D obj : structureFound.locations)
			{
				TileEntity tileEntity = obj.getTileEntity(pointer.getWorld());

				if(tileEntity instanceof TileEntityMultiblock && ((TileEntityMultiblock)tileEntity).cachedID != null)
				{
					idsFound.add(((TileEntityMultiblock)tileEntity).cachedID);
				}
			}

			MultiblockCache<T> cache = getNewCache();
			List<ItemStack> rejectedItems = new ArrayList<>();

			if(!idsFound.isEmpty())
			{
				for(String id : idsFound)
				{
					if(getManager().inventories.get(id) != null)
					{
						if(cache == null)
						{
							cache = getManager().pullInventory(pointer.getWorld(), id);
						}
						else {
							mergeCaches(rejectedItems, cache, getManager().pullInventory(pointer.getWorld(), id));
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
			structureFound.inventoryID = idToUse;
			
			onFormed();
			
			List<IStructuralMultiblock> structures = new ArrayList<>();
			Coord4D toUse = null;

			for(Coord4D obj : structureFound.locations)
			{
				TileEntity tileEntity = obj.getTileEntity(pointer.getWorld());

				if(tileEntity instanceof TileEntityMultiblock)
				{
					((TileEntityMultiblock<T>)tileEntity).structure = structureFound;
					
					if(toUse == null)
					{
						toUse = obj;
					}
				}
				else if(tileEntity instanceof IStructuralMultiblock)
				{
					structures.add((IStructuralMultiblock)tileEntity);
				}
			}
			
			//Remove all structural multiblocks from locations, set controllers
			for(IStructuralMultiblock node : structures)
			{
				node.setController(toUse);
				structureFound.locations.remove(Coord4D.get((TileEntity)node));
			}
		}
		else {
			for(Coord4D coord : iteratedNodes)
			{
				TileEntity tile = coord.getTileEntity(pointer.getWorld());
				
				if(tile instanceof TileEntityMultiblock)
				{
					TileEntityMultiblock<T> tileEntity = (TileEntityMultiblock<T>)tile;
					
					if(tileEntity.structure != null && !tileEntity.structure.destroyed)
					{
						onStructureDestroyed(tileEntity.structure);
						tileEntity.structure.destroyed = true;
					}
					
					tileEntity.structure = null;
				}
				else if(tile instanceof IStructuralMultiblock)
				{
					((IStructuralMultiblock)tile).setController(null);
				}
			}
			
			for(Coord4D coord : innerNodes)
			{
				killInnerNode(coord);
			}
		}
	}
	
	public class NodeCounter
	{
		public Set<Coord4D> iterated = new HashSet<>();
		
		public NodeChecker checker;
		
		public NodeCounter(NodeChecker c)
		{
			checker = c;
		}
		
		public void loop(Coord4D pos)
		{
			iterated.add(pos);
			
			if(!checker.shouldContinue(iterated.size()))
			{
				return;
			}
			
			for(EnumFacing side : EnumFacing.VALUES)
			{
				Coord4D coord = pos.offset(side);
				
				if(!iterated.contains(coord) && checker.isValid(coord))
				{
					loop(coord);
				}
			}
		}
		
		public int calculate(Coord4D coord)
		{
			if(!checker.isValid(coord))
			{
				return 0;
			}
			
			loop(coord);
			
			return iterated.size();
		}
	}
	
	public static abstract class NodeChecker
	{
		public abstract boolean isValid(final Coord4D coord);
		
		public boolean shouldContinue(int iterated)
		{
			return true;
		}
	}
}
