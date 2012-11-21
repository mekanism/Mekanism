package railcraft.common.api.crafting;

import net.minecraft.src.ItemStack;
import net.minecraftforge.liquids.LiquidStack;

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
