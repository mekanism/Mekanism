package railcraft.common.api.crafting;

import java.util.List;
import net.minecraft.src.ItemStack;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface IBlastFurnaceCraftingManager
{

    void addRecipe(int inputId, int inputDamage, int cookTime, ItemStack output);

    void addRecipe(int inputId, int cookTime, ItemStack output);

    List<ItemStack> getFuels();

    IBlastFurnaceRecipe getRecipe(int inputId, int inputDamage);

    IBlastFurnaceRecipe getRecipe(ItemStack stack);

    List<IBlastFurnaceRecipe> getRecipes();

}
