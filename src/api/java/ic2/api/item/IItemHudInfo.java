package ic2.api.item;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * An interface to allow the applied {@link Item} to override the overlay HUD text (which otherwise defaults to it's tooltip).
 *
 * @author Thunderdark
 */
public interface IItemHudInfo {
	/**
	 *	<p>Adds info to {@link IItemHudProvider}s for the given stack.
	 *	Return an empty list <b>not null</b> if no info is given.</p>
	 *
	 *	<pre><code>
	 *	{@literal @}override
	 *	public List{@literal <}String{@literal >} getHudInfo(ItemStack stack, boolean advanced) {
	 *		List{@literal <}String{@literal >} info = new LinkedList{@literal <}String{@literal >}();
	 *		info.add("I am a cool item");
	 *		info.add("and have cool info.");
	 * 		return info;
	 * 	}
	 *	</code></pre>
	 *
	 * @param stack The stack to get information for
	 * @param advanced Whether the HUD is in extended or advanced display mode
	 * @return A list of strings to draw in the overlay
	 */
	public List<String> getHudInfo(ItemStack stack, boolean advanced);
}