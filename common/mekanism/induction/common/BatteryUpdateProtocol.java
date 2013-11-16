package mekanism.induction.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.common.util.ListUtils;
import mekanism.induction.common.tileentity.TileEntityBattery;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

public class BatteryUpdateProtocol
{
	/** The battery nodes that have already been iterated over. */
	public Set<TileEntityBattery> iteratedNodes = new HashSet<TileEntityBattery>();

	/** The structures found, all connected by some nodes to the pointer. */
	public SynchronizedBatteryData structureFound = null;

	/** The original block the calculation is getting run from. */
	public TileEntity pointer;

	public BatteryUpdateProtocol(TileEntity tileEntity)
	{
		pointer = tileEntity;
	}

	private void loopThrough(TileEntity tile)
	{
		if(structureFound == null)
		{
			World worldObj = tile.worldObj;

			int origX = tile.xCoord, origY = tile.yCoord, origZ = tile.zCoord;

			boolean isCorner = true;
			boolean rightBlocks = true;

			Set<Vector3> locations = new HashSet<Vector3>();

			int xmin = 0, xmax = 0, ymin = 0, ymax = 0, zmin = 0, zmax = 0;

			int x = 0, y = 0, z = 0;

			if((isBattery(origX + 1, origY, origZ) && isBattery(origX - 1, origY, origZ)) || (isBattery(origX, origY + 1, origZ) && isBattery(origX, origY - 1, origZ)) || (isBattery(origX, origY, origZ + 1) && isBattery(origX, origY, origZ - 1)))
			{
				isCorner = false;
			}

			if(isCorner)
			{
				if(isBattery(origX + 1, origY, origZ))
				{
					xmin = 0;

					while (isBattery(origX + x + 1, origY, origZ))
					{
						x++;
					}

					xmax = x;
				}
				else {
					xmax = 0;

					while (isBattery(origX + x - 1, origY, origZ))
					{
						x--;
					}

					xmin = x;
				}

				if(isBattery(origX, origY + 1, origZ))
				{
					ymin = 0;

					while(isBattery(origX, origY + y + 1, origZ))
					{
						y++;
					}

					ymax = y;
				}
				else {
					ymax = 0;

					while(isBattery(origX, origY + y - 1, origZ))
					{
						y--;
					}

					ymin = y;
				}

				if(isBattery(origX, origY, origZ + 1))
				{
					zmin = 0;

					while(isBattery(origX, origY, origZ + z + 1))
					{
						z++;
					}

					zmax = z;
				}
				else {
					zmax = 0;

					while (isBattery(origX, origY, origZ + z - 1))
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
							if(!isBattery(origX + x, origY + y, origZ + z))
							{
								rightBlocks = false;
								break;
							}
							else {
								locations.add(new Vector3(tile).translate(new Vector3(x, y, z)));
							}
						}

						if(!rightBlocks)
						{
							break;
						}
					}

					if(!rightBlocks)
					{
						break;
					}
				}
			}

			if(locations.size() >= 1 && locations.size() < 512)
			{
				if(rightBlocks && isCorner)
				{
					SynchronizedBatteryData structure = new SynchronizedBatteryData();
					structure.locations = locations;

					if(structure.getVolume() > 1)
					{
						structure.isMultiblock = true;
					}

					if(structure.locations.contains(new Vector3(pointer)))
					{
						structureFound = structure;
					}
				}
			}
		}

		iteratedNodes.add((TileEntityBattery) tile);

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = new Vector3(tile).modifyPositionFromSide(side).getTileEntity(tile.worldObj);

			if(tileEntity instanceof TileEntityBattery)
			{
				if(!iteratedNodes.contains(tileEntity))
				{
					loopThrough(tileEntity);
				}
			}
		}
	}

	private boolean isBattery(int x, int y, int z)
	{
		if(pointer.worldObj.getBlockTileEntity(x, y, z) instanceof TileEntityBattery)
		{
			return true;
		}

		return false;
	}

	private void disperseCells()
	{
		Set<SynchronizedBatteryData> structures = new HashSet<SynchronizedBatteryData>();
		
		List<ItemStack> mergedInv = new ArrayList<ItemStack>();
		List<ItemStack[]> visibleInvs = new ArrayList<ItemStack[]>();

		for(TileEntityBattery tile : iteratedNodes)
		{
			structures.add(tile.structure);
		}
		
		for(SynchronizedBatteryData data : structures)
		{
			mergedInv = ListUtils.merge(mergedInv, data.inventory);
			
			if(data.hasVisibleInventory())
			{
				visibleInvs.add(data.visibleInventory);
			}
		}

		int maxCells = iteratedNodes.size() * BatteryManager.CELLS_PER_BATTERY;

		List<ItemStack> rejected = ListUtils.capRemains(mergedInv, maxCells);
		ejectItems(rejected, new Vector3(pointer));

		ArrayList<List<ItemStack>> inventories = ListUtils.split(ListUtils.cap(mergedInv, maxCells), iteratedNodes.size());
		List<TileEntityBattery> iterList = ListUtils.asList(iteratedNodes);

		for(int i = 0; i < iterList.size(); i++)
		{
			TileEntityBattery tile = iterList.get(i);
			tile.structure = SynchronizedBatteryData.getBase(tile, inventories.get(i));

			if(!visibleInvs.isEmpty())
			{
				tile.structure.visibleInventory = visibleInvs.get(0);
				visibleInvs.remove(0);
			}
		}
		
		if(!visibleInvs.isEmpty())
		{
			for(ItemStack[] inv : visibleInvs)
			{
				ejectItems(Arrays.asList(inv), new Vector3(pointer));
			}
		}
	}

	private void ejectItems(List<ItemStack> items, Vector3 vec)
	{
		for(ItemStack itemStack : items)
		{
			if(itemStack != null)
			{
				float motion = 0.7F;
				double motionX = (pointer.worldObj.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
				double motionY = (pointer.worldObj.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
				double motionZ = (pointer.worldObj.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
	
				EntityItem entityItem = new EntityItem(pointer.worldObj, vec.x + motionX, vec.y + motionY, vec.z + motionZ, itemStack);
	
				pointer.worldObj.spawnEntityInWorld(entityItem);
			}
		}
	}

	public void updateBatteries()
	{
		loopThrough(pointer);

		if(structureFound != null)
		{
			for(TileEntityBattery tileEntity : iteratedNodes)
			{
				if(!structureFound.locations.contains(new Vector3(tileEntity)))
				{
					disperseCells();

					return;
				}
			}
			
			boolean foundVisibleInv = false;

			for(Vector3 obj : structureFound.locations)
			{
				TileEntityBattery tileEntity = (TileEntityBattery)obj.getTileEntity(pointer.worldObj);

				structureFound.inventory = ListUtils.merge(structureFound.inventory, tileEntity.structure.inventory);

				if(tileEntity.structure.hasVisibleInventory())
				{
					if(foundVisibleInv)
					{
						ejectItems(Arrays.asList(tileEntity.structure.visibleInventory), obj);
					}
					else {
						structureFound.visibleInventory = tileEntity.structure.visibleInventory;
						foundVisibleInv = true;
					}
				}

				tileEntity.structure = structureFound;
			}

			List<ItemStack> rejected = ListUtils.capRemains(structureFound.inventory, structureFound.getMaxCells());
			ejectItems(rejected, new Vector3(pointer));

			structureFound.inventory = ListUtils.cap(structureFound.inventory, structureFound.getMaxCells());
		}
		else {
			disperseCells();
		}
	}
}
