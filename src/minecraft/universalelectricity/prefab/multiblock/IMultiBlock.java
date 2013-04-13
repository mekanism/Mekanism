package universalelectricity.prefab.multiblock;

import net.minecraft.tileentity.TileEntity;
import universalelectricity.core.vector.Vector3;

/**
 * Interface to be applied to tile entity blocks that occupies more than one block space. Useful for
 * large machines.
 * 
 * @author Calclavia
 * 
 */
public interface IMultiBlock extends IBlockActivate
{
	/**
	 * Called when this multiblock is created
	 * 
	 * @param placedPosition - The position the block was placed at
	 */
	public void onCreate(Vector3 placedPosition);

	/**
	 * Called when one of the multiblocks of this block is destroyed
	 * 
	 * @param callingBlock - The tile entity who called the onDestroy function
	 */
	public void onDestroy(TileEntity callingBlock);
}
