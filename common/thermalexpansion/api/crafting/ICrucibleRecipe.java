/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidStack;

public interface ICrucibleRecipe {

    public ItemStack getInput();

    public LiquidStack getOutput();

    public int getEnergy();
}
