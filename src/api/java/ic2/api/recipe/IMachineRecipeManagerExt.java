package ic2.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IMachineRecipeManagerExt extends IMachineRecipeManager {
	/**
	 * Adds a recipe to the machine.
	 *
	 * @note Overwrite is only as reliable as IRecipeInput.getInputs().
	 *
	 * @param input Recipe input
	 * @param metadata Meta data for additional recipe properties, may be null.
	 * @param overwrite Replace an existing recipe, not recommended, may be ignored.
	 * @param outputs Recipe outputs, zero or more depending on the machine.
	 * @return true on success, false otherwise, e.g. on conflicts.
	 *
	 * For the thermal centrifuge   @param metadata meta data {"minHeat": 1-xxx}
	 * For the ore washing plant  @param metadata meta data  {"amount": 1-8000}
	 */
	public boolean addRecipe(IRecipeInput input, NBTTagCompound metadata, boolean overwrite, ItemStack... outputs);
}
