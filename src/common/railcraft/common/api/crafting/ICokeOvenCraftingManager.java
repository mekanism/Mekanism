package railcraft.common.api.crafting;

import java.util.List;
import net.minecraft.src.ItemStack;
import net.minecraftforge.liquids.LiquidStack;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface ICokeOvenCraftingManager {

    void addRecipe(ItemStack input, ItemStack output, LiquidStack liquidOutput, int cookTime);

    void addRecipe(int inputId, int inputDamage, ItemStack output, LiquidStack liquidOutput, int cookTime);

    void addRecipe(int inputId, ItemStack output, LiquidStack liquidOutput, int cookTime);

    ICokeOvenRecipe getRecipe(ItemStack stack);

    ICokeOvenRecipe getRecipe(int inputId, int inputDamage);

    List<ICokeOvenRecipe> getRecipes();
}
