package mekanism.common.content.matrix;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityMultiblock;
import net.minecraft.item.ItemStack;

public class MatrixUpdateProtocol extends UpdateProtocol<SynchronizedMatrixData>
{
	public MatrixUpdateProtocol(TileEntityMultiblock<SynchronizedMatrixData> tileEntity) 
	{
		super(tileEntity);
	}

	@Override
	protected boolean isValidFrame(int x, int y, int z) 
	{
		return false;
	}

	@Override
	protected MatrixCache getNewCache() 
	{
		return new MatrixCache();
	}

	@Override
	protected SynchronizedMatrixData getNewStructure() 
	{
		return new SynchronizedMatrixData();
	}

	@Override
	protected MultiblockManager<SynchronizedMatrixData> getManager() 
	{
		return Mekanism.matrixManager;
	}

	@Override
	protected void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<SynchronizedMatrixData> cache, MultiblockCache<SynchronizedMatrixData> merge) 
	{
		
	}
}
