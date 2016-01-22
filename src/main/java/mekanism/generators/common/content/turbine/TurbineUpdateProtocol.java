package mekanism.generators.common.content.turbine;

import java.util.List;

import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.item.ItemStack;

public class TurbineUpdateProtocol extends UpdateProtocol<SynchronizedTurbineData>
{
	public TurbineUpdateProtocol(TileEntityTurbineCasing tileEntity) 
	{
		super(tileEntity);
	}

	@Override
	protected boolean isValidFrame(int x, int y, int z) 
	{
		return false;
	}

	@Override
	protected MultiblockCache<SynchronizedTurbineData> getNewCache() 
	{
		return new TurbineCache();
	}

	@Override
	protected SynchronizedTurbineData getNewStructure() 
	{
		return new SynchronizedTurbineData();
	}

	@Override
	protected MultiblockManager<SynchronizedTurbineData> getManager() 
	{
		return MekanismGenerators.turbineManager;
	}

	@Override
	protected void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<SynchronizedTurbineData> cache, MultiblockCache<SynchronizedTurbineData> merge)
	{
		
	}
}
