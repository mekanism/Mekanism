package cofh.api.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Implement this interface on subclasses of Item to change how the item works in Thermal Dynamics Fluiducts filter slots.
 *
 * This can be used to create customizable Items which are determined to be "equal" for the purposes of filtering.
 */
public interface ISpecialFilterFluid {

	/**
	 * This method is called to find out if the given FluidStack should be matched by the given Filter ItemStack.
	 *
	 * @param filter
	 *            ItemStack representing the filter.
	 * @param fluid
	 *            FluidStack representing the queried fluid.
	 * @return True if the filter should match the FluidStack. False if the default matching should be used.
	 */
	public boolean matchesFluid(ItemStack filter, FluidStack fluid);

}
