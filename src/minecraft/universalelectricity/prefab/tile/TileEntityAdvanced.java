package universalelectricity.prefab.tile;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

/**
 * A TileEntity with some pre-added functionalities.
 * 
 * @author Calclavia
 * 
 */
public abstract class TileEntityAdvanced extends TileEntity
{
	protected long ticks = 0;

	@Override
	public void updateEntity()
	{
		if (this.ticks == 0)
		{
			this.initiate();
		}

		if (this.ticks >= Long.MAX_VALUE)
		{
			this.ticks = 1;
		}

		this.ticks++;
	}

	/**
	 * Called on the TileEntity's first tick.
	 */
	public void initiate()
	{
	}

	@Override
	public int getBlockMetadata()
	{
		if (this.blockMetadata == -1)
		{
			this.blockMetadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		}

		return this.blockMetadata;
	}

	@Override
	public Block getBlockType()
	{
		if (this.blockType == null)
		{
			this.blockType = Block.blocksList[this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord)];
		}

		return this.blockType;
	}
}
