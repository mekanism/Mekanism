package cofh.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Implement this interface on Item classes which are Secure - linked to a specific player or group of players.
 * 
 * Obviously, this relies on people using this interface properly. The Wheaton Rule is in effect here - don't be a jerk.
 * 
 * @author King Lemming
 * 
 */
public interface ISecureItem {

	/**
	 * Check whether or not a given player can use this item.
	 */
	boolean canPlayerAccess(ItemStack stack, EntityPlayer player);

	/**
	 * Get the Owner of this item. This function is intentionally nebulous and is not guaranteed to be a player name.
	 */
	String getOwnerString();

}
