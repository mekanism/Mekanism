package universalelectricity.prefab.modifier;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * This slot should be used by any container that contains an item that is a modifier. An example of
 * this would be upgrade slots.
 * 
 * @author Calclavia
 * 
 */
public class SlotModifier extends Slot
{
	public SlotModifier(IInventory par2IInventory, int par3, int par4, int par5)
	{
		super(par2IInventory, par3, par4, par5);
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	 */
	@Override
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return par1ItemStack.getItem() instanceof IModifier;
	}
}
