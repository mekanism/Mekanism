/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

import net.minecraft.item.ItemStack;

public interface ISmelterRecipe {

    public ItemStack getPrimaryInput();

    public ItemStack getSecondaryInput();

    public ItemStack getPrimaryOutput();

    public ItemStack getSecondaryOutput();

    public int getSecondaryOutputChance();

    public int getEnergy();
}
