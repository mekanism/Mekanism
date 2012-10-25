package universalelectricity.prefab;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import universalelectricity.implement.IConductor;

public abstract class BlockConductor extends BlockContainer
{
	public BlockConductor(int id, Material material)
	{
		super(id, material);
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		super.onBlockAdded(world, x, y, z);
		
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		if(tileEntity != null)
		{
			if(tileEntity instanceof IConductor)
			{
				((IConductor)tileEntity).refreshConnectedBlocks();
			}
		}
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which
	 * neighbor changed (coordinates passed are their own) Args: x, y, z,
	 * neighbor blockID
	 */
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		if(tileEntity != null)
		{
			if(tileEntity instanceof IConductor)
			{
				((IConductor)tileEntity).refreshConnectedBlocks();
			}
		}
		
		world.markBlockNeedsUpdate(x, y, z);
	}
}
