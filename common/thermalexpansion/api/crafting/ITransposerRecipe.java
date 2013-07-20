/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ITransposerRecipe {

    public ItemStack getInput();

    public ItemStack getOutput();

    public FluidStack getLiquid();

    public int getEnergy();

    public int getChance();
}
