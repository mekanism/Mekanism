package mekanism.common.base;

import net.minecraft.block.Block;

/**
 * Implement if you have metadata-sensitive block rendering bounds.
 * @author aidancbrady
 *
 */
public interface ISpecialBounds
{
	/**
	 * Sets the render bounds for this particular block's subtype.
	 * @param block - the Block instance the renderer pertains to.
	 * @param metadata - metadata of the block being rendered
	 */
	public void setRenderBounds(Block block, int metadata);

	/**
	 * Whether or not to call the default setBlockBoundsForItemRender() before rendering this block as an item.
	 * @param metadata - metadata of the block being rendered
	 * @return whether or not to call default bound setting on this block's metadata.
	 */
	public boolean doDefaultBoundSetting(int metadata);
}
