/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

import net.minecraft.item.ItemStack;

public interface IFurnaceRecipe {

    public ItemStack getInput();

    public ItemStack getOutput();

    public int getEnergy();
}
