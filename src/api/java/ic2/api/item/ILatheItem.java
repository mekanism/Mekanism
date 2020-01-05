package ic2.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Interface used for Items that can be processed in the Turning Table
 */
public interface ILatheItem {

	/**
	 * Returns the radius of this Lathe Item
	 * Should be no more than 15 to still be able to display it properly.
	 */
	int getWidth(ItemStack stack);

	/**
	 * Returns the current state of this Lathe Item
	 * The length of the array should be 5
	 * The value should be the current size of the item at this time.
	 * It should also not be greater than getWidth()
	 */
	int[] getCurrentState(ItemStack stack);

	/**
	 * This will set the current state of the Item on that position to that value.
	 */
	void setState(ItemStack stack, int position, int value);

	/**
	 * Returns the output Item (normally dust) when you lathe on this position.
	 */
	ItemStack getOutputItem(ItemStack stack, int position);

	/**
	 * How common it is if the above item will be put into the output slot.
	 * 1.0 means it will always be outputted, 0.0 means it will never be outputted.
	 */
	float getOutputChance(ItemStack stack, int position);

	/**
	 * Returns the ResourceLocation of the texture used to display the item inside of the Lathe GUI
	 */
	@SideOnly(Side.CLIENT)
	ResourceLocation getTexture(ItemStack stack);

	/**
	 * This is similar to the Block HarvestLevel. Requires a different tool for a different hardness
	 * for ex. the Iron Turning Blank has a Hardness  of 2 (Like Iron Ore (Harvest Level)).
	 * In this case however it requires a Tool hardness of one above (3)
	 */
	int getHardness(ItemStack stack);

	/**
	 * Interface used for Tools that can be used to modify {@link ILatheItem}
	 */
	public static interface ILatheTool extends ICustomDamageItem {

		/**
		 * This is similar to the Tool HarvestLevel. Requires a different tool for a different hardness
		 * for ex. the Iron Turning Blank has a Hardness  of 2 (Like Iron Ore (Harvest Level)).
		 * The tool requires a hardness of one level above (in this case 3)
		 */
		int getHardness(ItemStack stack);
	}

}
