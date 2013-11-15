package cofh.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

/**
 * This interface is implemented on Item Conduits. Use it to attempt to eject items into an entry point.
 * 
 * @author Zeldo Kavira
 * 
 */
public interface IItemConduit {

	/**
	 * Insert items into the conduit. Returns the ItemStack left (null if fully routed). Will only accept items if they have an valid destination.
	 */
	public ItemStack sendItems(ItemStack item, ForgeDirection side);

}
