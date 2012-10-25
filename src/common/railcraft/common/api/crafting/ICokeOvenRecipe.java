package railcraft.common.api.crafting;

import buildcraft.api.liquids.LiquidStack;
import net.minecraft.src.ItemStack;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface ICokeOvenRecipe
{

    public int getCookTime();

    public ItemStack getInput();

    public LiquidStack getLiquidOutput();

    public ItemStack getOutput();
}
