package cofh.api.tileentity;

import net.minecraft.item.ItemStack;

/**
 * Implemented on TileEntities which support Augments.
 * 
 * @author King Lemming
 * 
 */
public interface IAugmentableTile {

	/**
	 * Attempt to reconfigure the tile based on the Augmentations present. Return TRUE if it was successful; FALSE if a condition was not met.
	 */
	boolean augmentTile();

	/**
	 * Returns an array of the Augment slots for this Tile Entity.
	 */
	ItemStack[] getAugmentSlots();

	/**
	 * Returns a status array for the Augmentations installed in the Tile Entity.
	 */
	boolean[] getAugmentStatus();

}
