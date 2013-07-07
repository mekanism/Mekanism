/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidStack;

public interface ITransposerRecipe {

    public ItemStack getInput();

    public ItemStack getOutput();

    public LiquidStack getLiquid();

    public int getEnergy();

    public int getChance();
}
