package mekanism.common.base;

/**
 * Internal interface.  A bounding block is not actually a 'bounding' block, it is really just a fake block that is
 * used to mimic actual block bounds.
 * @author AidanBrady
 *
 */
public interface IBoundingBlock
{
	/**
	 * Called when the main block is placed.
	 */
	public void onPlace();

	/**
	 * Called when any part of the structure is broken.
	 */
	public void onBreak();
}
