/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

import net.minecraft.item.ItemStack;

public interface IPulverizerRecipe {

    public ItemStack getInput();

    public ItemStack getPrimaryOutput();

    public ItemStack getSecondaryOutput();

    public int getSecondaryOutputChance();

    public int getEnergy();
}
