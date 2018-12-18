package mekanism.common.content.matrix;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.util.StackUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class MatrixUpdateProtocol extends UpdateProtocol<SynchronizedMatrixData>
{
	public MatrixUpdateProtocol(TileEntityInductionCasing tileEntity) 
	{
		super(tileEntity);
	}

	@Override
	protected boolean isValidFrame(int x, int y, int z) 
	{
		IBlockState state = pointer.getWorld().getBlockState(new BlockPos(x, y, z));
		return state.getBlock() == MekanismBlocks.BasicBlock2 && state.getBlock().getMetaFromState(state) == 1;
	}
	
	@Override
	public boolean isValidInnerNode(int x, int y, int z)
	{
		TileEntity tile = new Coord4D(x, y, z, pointer.getWorld().provider.getDimension()).getTileEntity(pointer.getWorld());
		
		if((tile instanceof TileEntityInductionCell || tile instanceof TileEntityInductionProvider))
		{
			return true;
		}
		
		return isAir(x, y, z);
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
		List<ItemStack> rejects = StackUtils.getMergeRejects(((MatrixCache)cache).inventory, ((MatrixCache)merge).inventory);
		
		if(!rejects.isEmpty())
		{
			rejectedItems.addAll(rejects);
		}
		
		StackUtils.merge(((MatrixCache)cache).inventory, ((MatrixCache)merge).inventory);
	}
	
	@Override
	protected boolean canForm(SynchronizedMatrixData structure)
	{
		for(Coord4D coord : innerNodes)
		{
			TileEntity tile = coord.getTileEntity(pointer.getWorld());
			
			if(tile instanceof TileEntityInductionCell)
			{
				structure.cells.add(coord);
				structure.storageCap += ((TileEntityInductionCell)tile).tier.maxEnergy;
			}
			else if(tile instanceof TileEntityInductionProvider)
			{
				structure.providers.add(coord);
				structure.transferCap += ((TileEntityInductionProvider)tile).tier.output;
			}
		}
		
		return true;
	}
}
