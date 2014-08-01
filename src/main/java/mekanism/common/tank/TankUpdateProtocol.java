package mekanism.common.tank;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TankUpdateProtocol
{
	public static final int FLUID_PER_TANK = 16000;

	/** The dynamic tank nodes that have already been iterated over. */
	public Set<TileEntityDynamicTank> iteratedNodes = new HashSet<TileEntityDynamicTank>();

	/** The structures found, all connected by some nodes to the pointer. */
	public SynchronizedTankData structureFound = null;

	/** The original block the calculation is getting run from. */
	public TileEntity pointer;

	public TankUpdateProtocol(TileEntity tileEntity)
	{
		pointer = tileEntity;
	}

	/**
	 * Recursively loops through each node connected to the given TileEntity.
	 * @param tile - the TileEntity to loop over
	 */
	public void loopThrough(TileEntity tile)
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
				SynchronizedTankData structure = new SynchronizedTankData();
				structure.locations = locations;
				structure.volLength = Math.abs(xmax-xmin)+1;
				structure.volHeight = Math.abs(ymax-ymin)+1;
				structure.volWidth = Math.abs(zmax-zmin)+1;
				structure.volume = volume;
				structure.renderLocation = Coord4D.get(tile).translate(0, 1, 0);

				for(Coord4D obj : structure.locations)
				{
					if(obj.getTileEntity(pointer.getWorldObj()) instanceof TileEntityDynamicValve)
					{
						ValveData data = new ValveData();
						data.location = obj;
						data.side = getSide(obj, origX+xmin, origX+xmax, origY+ymin, origY+ymax, origZ+zmin, origZ+zmax);

						structure.valves.add(data);
					}
				}

				if(structure.locations.contains(Coord4D.get(pointer)) && isCorrectCorner(Coord4D.get(tile), origX+xmin, origY+ymin, origZ+zmin))
				{
					structureFound = structure;
					return;
				}
			}
		}

		iteratedNodes.add((TileEntityDynamicTank)tile);

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = Coord4D.get(tile).getFromSide(side).getTileEntity(tile.getWorldObj());

			if(tileEntity instanceof TileEntityDynamicTank)
			{
				if(!iteratedNodes.contains(tileEntity))
				{
					loopThrough(tileEntity);
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
	private boolean isAir(int x, int y, int z)
	{
		return pointer.getWorldObj().isAirBlock(x, y, z);
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
		if(pointer.getWorldObj().getTileEntity(x, y, z) instanceof TileEntityDynamicTank)
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
		if(obj.xCoord == xmin && obj.yCoord == ymin && obj.zCoord == zmin)
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
	 * Whether or not the block at the specified location serves as a frame for a dynamic tank.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return
	 */
	private boolean isValidFrame(int x, int y, int z)
	{
		return pointer.getWorldObj().getBlock(x, y, z) == MekanismBlocks.BasicBlock && pointer.getWorldObj().getBlockMetadata(x, y, z) == 9;
	}

	/**
	 * Runs the protocol and updates all tanks that make a part of the dynamic tank.
	 */
	public void updateTanks()
	{
		loopThrough(pointer);

		if(structureFound != null)
		{
			for(TileEntityDynamicTank tileEntity : iteratedNodes)
			{
				if(!structureFound.locations.contains(Coord4D.get(tileEntity)))
				{
					for(TileEntity tile : iteratedNodes)
					{
						((TileEntityDynamicTank)tileEntity).structure = null;
					}

					return;
				}
			}

			List<Integer> idsFound = new ArrayList<Integer>();
			int idToUse = -1;

			for(Coord4D obj : structureFound.locations)
			{
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)obj.getTileEntity(pointer.getWorldObj());

				if(Mekanism.tankManager.getInventoryId(tileEntity) != -1)
				{
					idsFound.add(tileEntity.structure.inventoryID);
				}
			}

			DynamicTankCache cache = new DynamicTankCache();

			if(!idsFound.isEmpty())
			{
				for(int id : idsFound)
				{
					if(Mekanism.tankManager.inventories.get(id) != null)
					{
						cache = Mekanism.tankManager.pullInventory(pointer.getWorldObj(), id);
						idToUse = id;
						break;
					}
				}
			}
			else {
				idToUse = Mekanism.tankManager.getUniqueInventoryID();
			}

			cache.apply(structureFound);

			if(structureFound.fluidStored != null)
			{
				structureFound.fluidStored.amount = Math.min(structureFound.fluidStored.amount, structureFound.volume*FLUID_PER_TANK);
			}
			
			structureFound.inventoryID = idToUse;

			for(Coord4D obj : structureFound.locations)
			{
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)obj.getTileEntity(pointer.getWorldObj());

				tileEntity.structure = structureFound;
			}
		}
		else {
			for(TileEntity tileEntity : iteratedNodes)
			{
				((TileEntityDynamicTank)tileEntity).structure = null;
			}
		}
	}
}
