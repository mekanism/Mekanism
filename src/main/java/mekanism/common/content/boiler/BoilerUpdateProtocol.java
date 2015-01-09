package mekanism.common.content.boiler;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.util.StackUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.content.boiler.SynchronizedBoilerData.ValveData;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityBoiler;
import mekanism.common.tile.TileEntityBoilerValve;

import net.minecraft.item.ItemStack;

public class BoilerUpdateProtocol extends UpdateProtocol<SynchronizedBoilerData>
{
	public static final int WATER_PER_TANK = 16000;
	public static final int STEAM_PER_TANK = 160000;

	public BoilerUpdateProtocol(TileEntityBoiler tileEntity)
	{
		super(tileEntity);
	}

	@Override
	protected boolean isValidFrame(int x, int y, int z)
	{
		return pointer.getWorldObj().getBlock(x, y, z) == MekanismBlocks.BasicBlock2 && pointer.getWorldObj().getBlockMetadata(x, y, z) == 1;
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
			(structureFound).waterStored.amount = Math.min((structureFound).waterStored.amount, structureFound.volume*WATER_PER_TANK);
		}
		if((structureFound).steamStored != null)
		{
			(structureFound).steamStored.amount = Math.min((structureFound).waterStored.amount, structureFound.volume*STEAM_PER_TANK);
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
