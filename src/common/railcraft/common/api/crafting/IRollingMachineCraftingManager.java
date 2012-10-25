package railcraft.common.api.crafting;

import java.util.List;
import net.minecraft.src.IRecipe;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface IRollingMachineCraftingManager
{

    void addRecipe(ItemStack output, Object[] components);

    void addShapelessRecipe(ItemStack output, Object[] compenents);

    ItemStack findMatchingRecipe(InventoryCrafting inventorycrafting, World world);

    List<IRecipe> getRecipeList();

}
