/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ICrucibleRecipe {

    public ItemStack getInput();

    public FluidStack getOutput();

    public int getEnergy();
}
