package mekanism.common.content.tank;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.util.StackUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;

import net.minecraft.item.ItemStack;

public class TankUpdateProtocol extends UpdateProtocol<SynchronizedTankData>
{
	public static final int FLUID_PER_TANK = 16000;

	public TankUpdateProtocol(TileEntityDynamicTank tileEntity)
	{
		super(tileEntity);
	}

	@Override
	protected boolean isValidFrame(int x, int y, int z)
	{
		return pointer.getWorldObj().getBlock(x, y, z) == MekanismBlocks.BasicBlock && pointer.getWorldObj().getBlockMetadata(x, y, z) == 9;
	}
	
	@Override
	protected TankCache getNewCache()
	{
		return new TankCache();
	}
	
	@Override
	protected SynchronizedTankData getNewStructure()
	{
		return new SynchronizedTankData();
	}
	
	@Override
	protected MultiblockManager<SynchronizedTankData> getManager()
	{
		return Mekanism.tankManager;
	}
	
	@Override
	protected void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<SynchronizedTankData> cache, MultiblockCache<SynchronizedTankData> merge)
	{
		if(((TankCache)cache).fluid == null)
		{
			((TankCache)cache).fluid = ((TankCache)merge).fluid;
		}
		else if(((TankCache)merge).fluid != null && ((TankCache)cache).fluid.isFluidEqual(((TankCache)merge).fluid))
		{
			((TankCache)cache).fluid.amount += ((TankCache)merge).fluid.amount;
		}
		
		List<ItemStack> rejects = StackUtils.getMergeRejects(((TankCache)cache).inventory, ((TankCache)merge).inventory);
		
		if(!rejects.isEmpty())
		{
			rejectedItems.addAll(rejects);
		}
		
		StackUtils.merge(((TankCache)cache).inventory, ((TankCache)merge).inventory);
	}
	
	@Override
	protected void onFormed()
	{
		if(structureFound.fluidStored != null)
		{
			structureFound.fluidStored.amount = Math.min(structureFound.fluidStored.amount, structureFound.volume*FLUID_PER_TANK);
		}
	}
	
	@Override
	protected void onStructureCreated(SynchronizedTankData structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
	{
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
	}
}
