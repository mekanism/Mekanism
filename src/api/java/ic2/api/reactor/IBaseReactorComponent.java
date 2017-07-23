package ic2.api.reactor;

import net.minecraft.item.ItemStack;

/**
 * Items implementing this class will not get spilled out of the reactor, when they are in one.
 * @author Aroma1997
 */
public interface IBaseReactorComponent {

	/**
	 * This is to determine if the given Item can be put into a reactor.<br/>
	 * Items already in the reactor won't get spilled out. This only gets called,
	 * when placing the item in.
	 * @param stack The given reactor component.
	 * @param reactor The specific reactor to place the item in.
	 * @return If the item is accepted in the reactor.
	 */
	boolean canBePlacedIn(ItemStack stack, IReactor reactor);

}
